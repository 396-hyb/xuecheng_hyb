package com.xuecheng.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.mapper.XcChooseCourseMapper;
import com.xuecheng.learning.mapper.XcCourseTablesMapper;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.service.MyCourseTablesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author hyb
 * @version 1.0
 * @description 我的课程表接口实现类
 * @date 2025/1/17
 */
@Slf4j
@Service
public class MyCourseTablesServiceImpl implements MyCourseTablesService {

    @Autowired
    ContentServiceClient contentServiceClient;

    @Autowired
    XcChooseCourseMapper xcChooseCourseMapper;

    @Autowired
    XcCourseTablesMapper xcCourseTablesMapper;

    //添加选课
    @Override
    @Transactional
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId) {
        //查询课程信息
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);

        //课程收费标准
        String charge = coursepublish.getCharge();

        //选课记录
        XcChooseCourse chooseCourse = null;
        if("201000".equals(charge)){ //课程免费
            //添加免费课程
            chooseCourse  = addFreeCoruse(userId, coursepublish);
            //添加到我的课程表
            XcCourseTables xcCourseTables = addCourseTabls(chooseCourse);
        }else{
            //添加收费课程
            chooseCourse  = addChargeCoruse(userId, coursepublish);
        }
        XcChooseCourseDto xcChooseCourseDto = new XcChooseCourseDto();
        BeanUtils.copyProperties(chooseCourse, xcChooseCourseDto);
        //获取学习资格
        XcCourseTablesDto xcCourseTablesDto = getLearningStatus(userId, courseId);
        xcChooseCourseDto.setLearnStatus(xcCourseTablesDto.getLearnStatus());
        return xcChooseCourseDto;
    }

    //判断学习资格
    //学习资格: [{"code":"702001","desc":"正常学习"},{"code":"702002","desc":"没有选课或选课后没有支付"},{"code":"702003","desc":"已过期需要申请续期或重新支付"}]
    @Override
    public XcCourseTablesDto getLearningStatus(String userId, Long courseId) {
        //查询我的课程表
        XcCourseTables xcCourseTables = this.getXcCourseTables(userId, courseId);
        if(xcCourseTables == null){
            XcCourseTablesDto xcCourseTablesDto = new XcCourseTablesDto();
            //没有选课或选课后没有支付
            xcCourseTablesDto.setLearnStatus("702002");
            return xcCourseTablesDto;
        }
        XcCourseTablesDto xcCourseTablesDto = new XcCourseTablesDto();
        BeanUtils.copyProperties(xcCourseTables, xcCourseTablesDto);
        boolean isExpires = xcCourseTables.getValidtimeEnd().isBefore(LocalDateTime.now());
        //是否过期,true过期，false未过期
        if(isExpires){
            //已过期
            xcCourseTablesDto.setLearnStatus("702003");
        }else{
            //正常学习
            xcCourseTablesDto.setLearnStatus("702001");
        }
        return xcCourseTablesDto;
    }

    //添加免费课程
    private XcChooseCourse addFreeCoruse(String userId, CoursePublish coursepublish) {
        //查询选课记录表是否存在免费的且选课成功的订单
        Long courseId = coursepublish.getId();
        LambdaQueryWrapper<XcChooseCourse> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(XcChooseCourse::getUserId,userId)
                .eq(XcChooseCourse::getCourseId,courseId)
                .eq(XcChooseCourse::getOrderType,"700001") //免费课程
                .eq(XcChooseCourse::getStatus,"701001"); //选课成功
        List<XcChooseCourse> chooseCoursesList = xcChooseCourseMapper.selectList(lambdaQueryWrapper);
        if(chooseCoursesList != null && !chooseCoursesList.isEmpty()){
            return chooseCoursesList.get(0);
        }

        //添加选课记录信息
        XcChooseCourse chooseCourse = new XcChooseCourse();
        chooseCourse.setCourseId(courseId);
        chooseCourse.setCourseName(coursepublish.getName());
        chooseCourse.setUserId(userId);
        chooseCourse.setCompanyId(coursepublish.getCompanyId());
        chooseCourse.setOrderType("700001");
        chooseCourse.setCreateDate(LocalDateTime.now());
        chooseCourse.setCoursePrice(0f);
        chooseCourse.setValidDays(365);
        chooseCourse.setStatus("701001");
        chooseCourse.setValidtimeStart(LocalDateTime.now());
        chooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365L));
        xcChooseCourseMapper.insert(chooseCourse);
        return chooseCourse;
    }

    //添加到我的课程表
    private XcCourseTables addCourseTabls(XcChooseCourse chooseCourse) {
        //选课记录完成且未过期可以添加课程到课程表
        String status = chooseCourse.getStatus();
        if(!"701001".equals(status)){
            XueChengPlusException.cast("选课未成功，无法添加到课程表");
        }
        //查询我的课程表
        String userId = chooseCourse.getUserId();
        Long courseId = chooseCourse.getCourseId();
        XcCourseTables xcCourseTables = getXcCourseTables(userId,courseId);
        if(xcCourseTables != null){
            return xcCourseTables;
        }
        //插入到课程表
        XcCourseTables xcCourseTablesNew = new XcCourseTables();
        xcCourseTablesNew.setChooseCourseId(chooseCourse.getId());
        xcCourseTablesNew.setUserId(userId);
        xcCourseTablesNew.setCourseId(courseId);
        xcCourseTablesNew.setCompanyId(chooseCourse.getCompanyId());
        xcCourseTablesNew.setCourseName(chooseCourse.getCourseName());
        xcCourseTablesNew.setCreateDate(LocalDateTime.now());
        xcCourseTablesNew.setValidtimeStart(chooseCourse.getValidtimeStart());
        xcCourseTablesNew.setValidtimeEnd(chooseCourse.getValidtimeEnd());
        xcCourseTablesNew.setCourseType(chooseCourse.getOrderType());
        xcCourseTablesMapper.insert(xcCourseTablesNew);
        return xcCourseTablesNew;
    }

    /**
     * 查询我的课程表
     */
    private XcCourseTables getXcCourseTables(String userId, Long courseId) {
        LambdaQueryWrapper<XcCourseTables> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(XcCourseTables::getUserId, userId)
                .eq(XcCourseTables::getCourseId, courseId);
        return xcCourseTablesMapper.selectOne(queryWrapper);
    }

    //添加收费课程
    private XcChooseCourse addChargeCoruse(String userId, CoursePublish coursepublish) {
        //如果存在待支付交易记录直接返回
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper = queryWrapper.eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, coursepublish.getId())
                .eq(XcChooseCourse::getOrderType, "700002")//收费订单
                .eq(XcChooseCourse::getStatus, "701002");//待支付
        List<XcChooseCourse> xcChooseCourses = xcChooseCourseMapper.selectList(queryWrapper);
        if (xcChooseCourses != null && xcChooseCourses.size()>0) {
            return xcChooseCourses.get(0);
        }

        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(coursepublish.getId());
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setCoursePrice(coursepublish.getPrice());
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
        xcChooseCourse.setOrderType("700002");//收费课程
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setStatus("701002");//待支付

        xcChooseCourse.setValidDays(coursepublish.getValidDays());
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(coursepublish.getValidDays()));
        xcChooseCourseMapper.insert(xcChooseCourse);
        return xcChooseCourse;

    }
}
