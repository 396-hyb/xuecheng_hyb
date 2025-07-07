package com.xuecheng.learning.service;

import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;

/**
 * @author hyb
 * @version 1.0
 * @description 我的课程表接口
 * @date 2025/1/17
 */
public interface MyCourseTablesService {

    /**
     * 添加选课
     * @param userId 用户id
     * @param courseId 课程id
     * @return com.xuecheng.learning.model.dto.XcChooseCourseDto
     */
    XcChooseCourseDto addChooseCourse(String userId, Long courseId);


    /**
     * 判断学习资格
     * @param userId 用户id
     * @param courseId 课程id
     * @return com.xuecheng.learning.model.dto.XcCourseTablesDto
     */
    XcCourseTablesDto getLearningStatus(String userId, Long courseId);
}
