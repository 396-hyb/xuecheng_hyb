package com.xuecheng.orders.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.orders.mapper.XcSeckillVoucherMapper;
import com.xuecheng.orders.mapper.XcVoucherMapper;
import com.xuecheng.orders.mapper.XcVoucherOrderMapper;
import com.xuecheng.orders.model.dto.UserDTO;
import com.xuecheng.orders.model.dto.VoucherDto;
import com.xuecheng.orders.model.dto.VoucherOrderDto;
import com.xuecheng.orders.service.VoucherOrderService;
import com.xuecheng.orders.util.RedisIdWorker;
import com.xuecheng.orders.util.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;

@Slf4j
@Service
public class VoucherOrderServiceImpl implements VoucherOrderService {

    @Resource
    private XcVoucherOrderMapper xcVoucherOrderMapper;

    @Resource
    private VoucherOrderService voucherOrderService;

    @Resource
    private XcVoucherMapper xcVoucherMapper;

    // 分布式ID生成器
    @Resource
    private RedisIdWorker redisIdWorker;

    // Redisson客户端（分布式锁）
    @Resource
    private RedissonClient redissonClient;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    @Override
    public Long seckillVoucher(Long voucherId) {
        Long userId = UserHolder.getUser().getId();
        long orderId = redisIdWorker.nextId("order");
        // 1.执行lua脚本
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(), userId.toString(), String.valueOf(orderId)
        );
        int r = result.intValue();
        // 2.判断结果是否为0
        if (r != 0) {
            // 2.1.不为0 ，代表没有购买资格
            XueChengPlusException.cast(r == 1 ? "库存不足" : "不能重复下单");
        }

        // 3.发送消息到RocketMQ，由消费者异步处理订单创建
        VoucherOrderDto voucherOrder = new VoucherOrderDto();
        voucherOrder.setId(orderId);
        voucherOrder.setUserId(userId);
        voucherOrder.setVoucherId(voucherId);

        // 发送异步消息
        rocketMQTemplate.asyncSend("topic-voucher-order", voucherOrder, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("消息发送成功：{}", sendResult.getMsgId());
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("消息发送失败", throwable);
            }
        });

        //3.获取代理对象，这里会失效
        // AopContext.currentProxy() 只能在原始请求线程中获取当前代理对象，而在消费者线程中调用时，这个上下文已经不存在。
        // proxy = (VoucherOrderService) AopContext.currentProxy();

        // 4.快速响应：立即返回订单ID，避免用户长时间等待数据库操作
        return orderId;
    }

    // RocketMQ消费者，使用@RocketMQMessageListener注解配置
    @Component
    @RocketMQMessageListener(
            consumerGroup = "voucher-order-consumer",
            topic = "topic-voucher-order",
            consumeMode = ConsumeMode.CONCURRENTLY // 并发消费模式
    )
    public class VoucherOrderConsumer implements RocketMQListener<VoucherOrderDto> {

        @Resource
        private VoucherOrderService voucherOrderService;

        @Resource
        private RedissonClient redissonClient;

        @Override
        public void onMessage(VoucherOrderDto voucherOrder) {
            try {
                // 创建订单
                handleVoucherOrder(voucherOrder);
            } catch (Exception e) {
                log.error("处理订单异常", e);
                // 可以实现重试逻辑或记录失败订单
            }
        }

        private void handleVoucherOrder(VoucherOrderDto voucherOrder) {
            //1.获取用户
            Long userId = voucherOrder.getUserId();
            // 2.创建锁对象
            RLock redisLock = redissonClient.getLock("lock:order:" + userId);
            // 3.尝试获取锁
            boolean isLock = redisLock.tryLock();
            // 4.判断是否获得锁成功
            if (!isLock) {
                // 获取锁失败，直接返回失败或者重试
                log.error("不允许重复下单");
                return;
            }
            try {
                // 通过注入的Service调用事务方法
                voucherOrderService.createVoucherOrder(voucherOrder);
            } finally {
                // 释放锁
                redisLock.unlock();
            }
        }
    }

    @Transactional
    public void createVoucherOrder(VoucherOrderDto voucherOrder) {
        // 1.一人一单逻辑
        // 1.1.用户id
        Long userId = voucherOrder.getUserId();
        Long voucherId = voucherOrder.getVoucherId();

        // 5.1.查询订单
        LambdaQueryWrapper<VoucherDto> lambdaQuery = new LambdaQueryWrapper<>();
        int count = lambdaQuery.eq(UserDTO::getId,
                userId).eq(VoucherDto::getId, voucherId).count();
        // 5.2.判断是否存在
        if (count > 0) {
            // 用户已经购买过了
            log.error("不允许重复下单！");
            return;
        }

        // 6.扣减库存
        boolean success = xcVoucherMapper.update()
                .setSql("stock = stock - 1") // set stock = stock - 1
                .eq("voucher_id", voucherId).gt("stock", 0) // where id = ? and stock > 0
                .update();
        if (!success) {
            // 扣减失败
            log.error("库存不足！");
            return;
        }

        // 7.创建订单
        xcVoucherOrderMapper.insert(voucherOrder);
    }
}
