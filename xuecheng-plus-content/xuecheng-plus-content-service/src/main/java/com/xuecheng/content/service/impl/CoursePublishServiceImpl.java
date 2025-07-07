package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import com.xuecheng.content.feignclient.SearchServiceClient;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CourseIndex;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.CourseTeacherService;
import com.xuecheng.content.service.TeachplanService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

/**
 * @author hyb
 * @version 1.0
 * @description 课程预览、发布接口实现类
 * @date 2024/12/20
 */
@Slf4j
@Service
public class CoursePublishServiceImpl implements CoursePublishService {

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @Autowired
    TeachplanService teachplanService;

    @Autowired
    CourseMarketMapper courseMarketMapper;

    @Autowired
    CourseTeacherService courseTeacherService;

    @Autowired
    CoursePublishPreMapper coursePublishPreMapper;

    @Autowired
    CoursePublishMapper coursePublishMapper;

    @Autowired
    MqMessageService mqMessageService;

    @Autowired
    MediaServiceClient mediaServiceClient;

    @Autowired
    SearchServiceClient searchServiceClient;

    //得到课程预览信息
    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        //课程基本信息、营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);

        //课程计划信息
        List<TeachplanDto> teachplanTree = teachplanService.selectTreeNodes(courseId);

        CoursePreviewDto coursePreview = new CoursePreviewDto();
        coursePreview.setCourseBase(courseBaseInfo);
        coursePreview.setTeachplans(teachplanTree);

        return coursePreview;
    }

    //提交审核
    @Transactional
    @Override
    public void commitAudit(Long companyId,Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        String auditStatus = courseBase.getAuditStatus();
        //约束：
        //对已提交审核的课程不允许提交审核。
        if("202003".equals(auditStatus)){
            XueChengPlusException.cast("当前为等待审核状态，审核完成可以再次提交。");
        }
        //本机构只允许提交本机构的课程。
        if(companyId.compareTo(courseBase.getCompanyId()) != 0){
            XueChengPlusException.cast("不允许提交其它机构的课程。");
        }
        //没有上传图片不允许提交审核。
        if(StringUtils.isEmpty(courseBase.getPic())){
            XueChengPlusException.cast("提交失败，请上传课程图片");
        }
        //没有添加课程计划不允许提交审核。
        List<TeachplanDto> teachplanTree = teachplanService.selectTreeNodes(courseId);
        if(teachplanTree == null || teachplanTree.isEmpty()){
            XueChengPlusException.cast("提交失败，还没有添加课程计划");
        }
        //添加课程预发布记录
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        //课程基本信息和部分营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        BeanUtils.copyProperties(courseBaseInfo, coursePublishPre);
        //课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        String courseMarketJson = JSON.toJSONString(courseMarket);
        coursePublishPre.setMarket(courseMarketJson);
        //课程计划
        String teachplanTreeJson = JSON.toJSONString(teachplanTree);
        coursePublishPre.setTeachplan(teachplanTreeJson);
        //课程老师
        List<CourseTeacher> courseTeacherList = courseTeacherService.selectCourseTeacher(courseId);
        String courseTeacherListJson = JSON.toJSONString(courseTeacherList);
        coursePublishPre.setTeachers(courseTeacherListJson);

        //设置提交时间
        coursePublishPre.setCreateDate(LocalDateTime.now());
        //设置预发布记录状态,已提交
        coursePublishPre.setStatus("202003");
        //教学机构id
        coursePublishPre.setCompanyId(companyId);

        //向课程预发布表course_publish_pre插入一条记录，如果已经存在则更新，审核状态为：已提交。
        CoursePublishPre coursePublishPreUpdate = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPreUpdate == null){
            //插入
            coursePublishPreMapper.insert(coursePublishPre);
        }else{
            //跟新
            coursePublishPreMapper.updateById(coursePublishPre);
        }
        //更新课程基本表course_base课程审核状态为：已提交。
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);
    }

    //课程发布
    @Transactional
    @Override
    public void publish(Long companyId, Long courseId) {
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);

        //约束
        if(coursePublishPre == null){
            XueChengPlusException.cast("该课程未提交审核，不允许发布");
        }
        if(!companyId.equals(coursePublishPre.getCompanyId())){
            XueChengPlusException.cast("该课程非自己机构提交，不允许发布");
        }
        if(!"202004".equals(coursePublishPre.getStatus())){
            XueChengPlusException.cast("该课程审核未通过，不允许发布");
        }

        //保存课程发布信息
        saveCoursePublish(courseId);

        //向 mq_message 消息表插入消息，消息类型为：course_publish
        saveCoursePublishMessage(courseId);

        //删除课程预发布表 course_publish_pre 记录
        coursePublishPreMapper.deleteById(courseId);
    }


    /**
     * 保存课程发布信息
     * @param courseId 课程id
     */
    private void saveCoursePublish(Long courseId) {
        //查询课程预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPre == null){
            XueChengPlusException.cast("课程预发布数据为空");
        }
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre, coursePublish);
        //状态更新为发布
        coursePublish.setStatus("203002");
        CoursePublish coursePublishUpdate = coursePublishMapper.selectById(courseId);
        if(coursePublishUpdate == null){
            //插入
            coursePublishMapper.insert(coursePublish);
        }else{
            //跟新
            coursePublishMapper.updateById(coursePublish);
        }
        //更新 course_Base 表发布状态
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setStatus("203002");
        int i = courseBaseMapper.updateById(courseBase);
        if(i < 1){
            XueChengPlusException.cast("course_Base表更新发布状态失败");
        }
    }

    /**
     * 保存消息表记录
     * @param courseId  课程id
     */
    private void saveCoursePublishMessage(Long courseId){
        MqMessage message = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if(message == null){
            XueChengPlusException.cast(CommonError.UNKOWN_ERROR);
        }
    }

    @Override
    public File generateCourseHtml(Long courseId) {

        File htmlFile = null;

        try {
            Configuration configuration = new Configuration(Configuration.getVersion());

            //拿到classpath路径
            String classpath = this.getClass().getResource("/").getPath();
            //指定模板的目录
            configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
            //指定编码
            configuration.setDefaultEncoding("utf-8");

            //得到模板
            Template template = configuration.getTemplate("course_template.ftl");
            //准备数据
            CoursePreviewDto coursePreviewInfo = this.getCoursePreviewInfo(courseId);
            HashMap<String, Object> map = new HashMap<>();
            map.put("model",coursePreviewInfo);

            //Template template 模板, Object model 数据
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
            //输入流
            InputStream inputStream = IOUtils.toInputStream(html, "utf-8");
            //输出文件
            htmlFile = File.createTempFile("course", ".html");
            log.debug("课程静态化，生成静态文件:{}",htmlFile.getAbsolutePath());
            FileOutputStream outputStream = new FileOutputStream(htmlFile);
            //使用流将html写入文件
            IOUtils.copy(inputStream,outputStream);
        } catch (Exception e) {
            log.error("课程静态化异常:{}",e.toString());
            XueChengPlusException.cast("课程静态化异常");
        }

        return htmlFile;
    }

    //上传课程静态页面
    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        try {
            MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
            String course = mediaServiceClient.upload(multipartFile, "course/" + courseId + ".html");
            if(course == null){
                log.debug("远程调用走降级逻辑得到上传的结果为null,课程id:{}",courseId);
                XueChengPlusException.cast("上传静态文件过程中出现异常");
            }
        } catch (Exception e) {
            log.error("课程静态html页面上传minio异常:{}",e.getMessage());
            XueChengPlusException.cast("上传静态文件过程中出现异常");
        }
    }

    //保存课程索引信息
    @Override
    public Boolean saveCourseIndex(Long courseId) {
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        CourseIndex courseIndex = new CourseIndex();
        BeanUtils.copyProperties(coursePublish, courseIndex);
        Boolean add = searchServiceClient.add(courseIndex);
        if(!add){
            XueChengPlusException.cast("添加索引失败");
        }
        return add;
    }

    //获取课程发布信息
    @Override
    public CoursePublish getCoursePublish(Long courseId) {
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        return coursePublish;
    }
}
