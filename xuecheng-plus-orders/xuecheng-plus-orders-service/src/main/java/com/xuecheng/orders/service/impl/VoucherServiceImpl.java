package com.xuecheng.orders.service.impl;

import com.alipay.api.domain.Voucher;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.orders.mapper.XcSeckillVoucherMapper;
import com.xuecheng.orders.mapper.XcVoucherMapper;
import com.xuecheng.orders.model.dto.SeckillVoucherDto;
import com.xuecheng.orders.model.dto.VoucherDto;
import com.xuecheng.orders.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import static com.xuecheng.orders.util.RedisConstants.SECKILL_STOCK_KEY;

@Service
public class VoucherServiceImpl implements VoucherService {
    @Autowired
    XcVoucherMapper xcVoucherMapper;

    @Autowired
    XcSeckillVoucherMapper xcSeckillVoucherMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public List<VoucherDto> queryVoucherOfShop(Long shopId) {
        // 查询优惠券信息
        LambdaQueryWrapper<VoucherDto> lambdaQuery = new LambdaQueryWrapper<>();
        lambdaQuery.eq(VoucherDto::getShopId, shopId)
                .orderByDesc(VoucherDto::getCreateTime);

        return xcVoucherMapper.selectList(lambdaQuery);
        // 返回结果
    }

    @Override
    public void addVoucher(VoucherDto voucher) {
        int insert = xcVoucherMapper.insert(voucher);
        if(insert <= 0){
            throw new XueChengPlusException("新增普通优惠券失败");
        }
    }

    @Override
    public void addSeckillVoucher(VoucherDto voucher) {

        // 保存优惠券
        addVoucher(voucher);
        // 保存秒杀信息
        SeckillVoucherDto seckillVoucher = new SeckillVoucherDto();
        seckillVoucher.setVoucherId(voucher.getId());
        seckillVoucher.setStock(voucher.getStock());
        seckillVoucher.setBeginTime(voucher.getBeginTime());
        seckillVoucher.setEndTime(voucher.getEndTime());
        int insert = xcSeckillVoucherMapper.insert(seckillVoucher);
        if(insert <= 0){
            throw new XueChengPlusException("新增秒杀优惠券失败");
        }
        // 保存秒杀库存到Redis中
        stringRedisTemplate.opsForValue().set(SECKILL_STOCK_KEY + voucher.getId(), voucher.getStock().toString());
    }
}
