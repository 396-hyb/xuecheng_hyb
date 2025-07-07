package com.xuecheng.messagesdk;

import com.xuecheng.messagesdk.mapper.MqMessageMapper;
import com.xuecheng.messagesdk.model.po.MqMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

/**
 * @author hyb
 * @version 1.0
 * @description 消息处理SDK测试类
 * @date 2024/12/21
 */
@SpringBootTest
public class MessageProcessClassTest {
    @Autowired
    MessageProcessClass messageProcessClass;

    @Autowired
    MqMessageMapper mqMessageMapper;

    @Test
    public void insertTestData(){
        MqMessage message = new MqMessage();
        message.setMessageType("test");
        mqMessageMapper.insert(message);
        System.out.println("测试数据插入成功");
    }


    @Test
    public void test() throws InterruptedException {

        System.out.println("开始执行-----》" + LocalDateTime.now());
        messageProcessClass.process(0, 1, "test", 5, 30);
        System.out.println("结束执行-----》" + LocalDateTime.now());
        Thread.sleep(9000000);
    }


}
