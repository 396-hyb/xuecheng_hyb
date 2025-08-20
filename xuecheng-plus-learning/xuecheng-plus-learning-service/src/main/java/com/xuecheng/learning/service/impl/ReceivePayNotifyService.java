package com.xuecheng.learning.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.learning.config.PayNotifyConfig;
import com.xuecheng.learning.service.MyCourseTablesService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 接收消息通知处理类
 */
@Slf4j
@Service
@RocketMQMessageListener(
        topic = PayNotifyConfig.PAYNOTIFY_TOPIC,
        consumerGroup = PayNotifyConfig.CONSUMER_GROUP,
        selectorExpression = PayNotifyConfig.PAYNOTIFY_TAG,
        maxReconsumeTimes = 3 // 最大重试次数
)
public class ReceivePayNotifyService implements RocketMQListener<String> {
    @Autowired
    MyCourseTablesService myCourseTablesService;

    @Override
    public void onMessage(String message) {
        log.info("接收到支付通知消息: {}", message);

        try {
            //转成对象
            MqMessage mqMessage = JSON.parseObject(message, MqMessage.class);
            //解析消息的内容
            //选课id
            String chooseCourseId = mqMessage.getBusinessKey1();
            //订单类型
            String orderType = mqMessage.getBusinessKey2();

            log.info("处理支付通知 - 选课ID: {}, 订单类型: {}", chooseCourseId, orderType);

            //学习中心服务只要购买课程类的支付订单的结果
            if ("60201".equals(orderType)) {
                //根据消息内容，更新选课记录、向我的课程表插入记录
                boolean success = myCourseTablesService.saveChooseCourseSuccess(chooseCourseId);
                if (!success) {
                    log.error("保存选课记录状态失败 - 选课ID: {}", chooseCourseId);
                    throw new RuntimeException("保存选课记录状态失败");
                }
                log.info("成功处理支付通知 - 选课ID: {}", chooseCourseId);
            }
        } catch (Exception e) {
            log.error("处理支付通知消息失败: {}", message, e);
            // 抛出异常，让RocketMQ进行重试
            throw new RuntimeException("处理支付通知消息失败", e);
        }
    }
}
