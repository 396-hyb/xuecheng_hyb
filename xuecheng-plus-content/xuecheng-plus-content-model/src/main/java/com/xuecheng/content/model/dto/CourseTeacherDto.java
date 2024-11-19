package com.xuecheng.content.model.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

/**
 * @author hyb
 * @version 1.0
 * @description 保存课程老师dto，包括新增、修改。如果传递了课程老师id说明当前是要修改，否则是新增。
 * @date 2024/11/19
 */
@Data
public class CourseTeacherDto {
    /**
     * 课程老师id
     */
    private Long id;

    /**
     * 课程标识
     */
    private Long courseId;

    /**
     * 教师标识
     */
    @NotEmpty(message = "教师姓名不能为空")
    private String teacherName;

    /**
     * 教师职位
     */
    @NotEmpty(message = "教师职位不能为空")
    private String position;

    /**
     * 教师简介
     */
    @Size(message = "教师简介内容过少,不能少于10个字", min = 10)
    private String introduction;

/*    *//**
     * 照片
     *//*
    private String photograph;

    *//**
     * 创建时间
     *//*
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createDate*/

}
