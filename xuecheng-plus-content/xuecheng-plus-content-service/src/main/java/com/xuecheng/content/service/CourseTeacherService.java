package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseTeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

/**
 * @author hyb
 * @version 1.0
 * @description 课程老师Service
 * @date 2024/11/18
 */
public interface CourseTeacherService {

    /**
     * 查询课程老师信息
     * @param courseId 课程id
     */
    List<CourseTeacher> selectCourseTeacher(Long courseId);

    /**
     * 课程老师创建或修改
     * @param companyId
     * @param courseTeacherDto
     * @return
     */
    CourseTeacher saveCourseTeacher(Long companyId, CourseTeacherDto courseTeacherDto);

    /**
     * 删除课程老师
     * @param courseId 课程id
     * @param id  教师id，即course_teacher表的主键
     */
    void deleteCourseTeacher(Long courseId, Long id);
}

