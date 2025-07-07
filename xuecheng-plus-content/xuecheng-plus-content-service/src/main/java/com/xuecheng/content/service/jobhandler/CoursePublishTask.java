package com.xuecheng.content.service.jobhandler;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * @author hyb
 * @version 1.0
 * @description 课程发布任务处理执行类
 * @date 2024/12/21
 */

@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {

    @Autowired
    CoursePublishService coursePublishService;

    @Autowired
    CoursePublishMapper coursePublishMapper;

    //任务调度入口
    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.debug("shardIndex="+shardIndex+",shardTotal="+shardTotal);
        //参数:分片序号、分片总数、消息类型、一次最多取到的任务数量、一次任务调度执行的超时时间
//处理视频非常消耗性能的任务才走CPU核数,这里不用
        process(shardIndex,shardTotal,"course_publish",30,60);
    }

    //课程发布任务处理
    @Override
    public boolean execute(MqMessage mqMessage) {
        String businessKey1 = mqMessage.getBusinessKey1();
        Long courseId = Long.parseLong(businessKey1);
        //课程静态化
        generateCourseHtml(mqMessage,courseId);
        //课程索引
        saveCourseIndex(mqMessage,courseId);
        //课程缓存
        saveCourseCache(mqMessage,courseId);
        return true;
    }

    //生成课程静态化页面并上传至文件系统
    private void generateCourseHtml(MqMessage mqMessage, Long courseId) {
        log.debug("开始进行课程静态化,课程id:{}",courseId);
        //消息id
        Long messageId = mqMessage.getId();
        //消息处理的service
        MqMessageService mqMessageService = this.getMqMessageService();
        //消息幂等性处理
        int stageOne = mqMessageService.getStageOne(messageId);
        if(stageOne > 0){
            log.debug("课程静态化已处理直接返回，课程id:{}",courseId);
            return ;
        }
        File file = coursePublishService.generateCourseHtml(courseId);
        if(file == null){
            XueChengPlusException.cast("html页面为空");
        }
        coursePublishService.uploadCourseHtml(courseId, file);
        //保存第一阶段状态
        mqMessageService.completedStageOne(messageId);
    }

    //将课程信息缓存至redis
    public void saveCourseCache(MqMessage mqMessage,Long courseId){
        log.debug("将课程信息缓存至redis,课程id:{}",courseId);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //保存课程索引信息
    public void saveCourseIndex(MqMessage mqMessage,Long courseId){
        log.debug("保存课程索引信息,课程id:{}",courseId);

        //消息id
        Long messageId = mqMessage.getId();
        //消息处理的service
        MqMessageService mqMessageService = this.getMqMessageService();
        //消息幂等性处理
        int stageTwo = mqMessageService.getStageTwo(messageId);
        if(stageTwo > 0){
            log.debug("课程索引信息已保存,直接返回,课程id:{}",courseId);
            return ;
        }
        Boolean result = coursePublishService.saveCourseIndex(courseId);
        if(result){
            mqMessageService.completedStageTwo(messageId);
        }

    }
}
