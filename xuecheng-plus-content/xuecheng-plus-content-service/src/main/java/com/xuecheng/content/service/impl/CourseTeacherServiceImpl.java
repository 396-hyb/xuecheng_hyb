package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.dto.CourseTeacherDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author hyb
 * @version 1.0
 * @description 课程老师Service实现类
 * @date 2024/11/18
 */
@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {

    @Autowired
    CourseTeacherMapper courseTeacherMapper;

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Override
    public List<CourseTeacher> selectCourseTeacher(Long courseId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseId);
        List<CourseTeacher> courseTeacherList = courseTeacherMapper.selectList(queryWrapper);
        //可以直接返回null
        return courseTeacherList;
    }

    @Transactional
    @Override
    public CourseTeacher saveCourseTeacher(Long companyId, CourseTeacherDto courseTeacherDto) {
        Long id = courseTeacherDto.getId(); //课程老师id
        Long courseId = courseTeacherDto.getCourseId();

        //验证课程是否存在，存在才添加老师
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase == null){
            XueChengPlusException.cast("没有该课程，无法添加老师");
        }

        CourseTeacher courseTeacher = new CourseTeacher();
        if(id == null){
            //创建
            BeanUtils.copyProperties(courseTeacherDto, courseTeacher);
            courseTeacher.setCreateDate(LocalDateTime.now());
            courseTeacherMapper.insert(courseTeacher);
        }else{
            //修改
            courseTeacher = courseTeacherMapper.selectById(id);
            BeanUtils.copyProperties(courseTeacherDto,courseTeacher);
            courseTeacherMapper.updateById(courseTeacher);
        }
        return courseTeacher;
    }

    @Transactional
    @Override
    public void deleteCourseTeacher(Long courseId, Long id) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getId,id).eq(CourseTeacher::getCourseId,courseId);
        int delete = courseTeacherMapper.delete(queryWrapper);
        if(delete < 1){
            XueChengPlusException.cast("删除失败");
        }
    }

}
