package com.xuecheng.learning.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class PayNotifyConfig {
    //主题名称
    public static final String PAYNOTIFY_TOPIC = "paynotify_topic";
    //支付结果通知消息类型
    public static final String MESSAGE_TYPE = "payresult_notify";
    //标签
    public static final String PAYNOTIFY_TAG = "paynotify_tag";
    //消费者组
    public static final String CONSUMER_GROUP = "paynotify_consumer_group";
}
