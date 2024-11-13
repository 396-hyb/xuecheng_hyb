package com.xuecheng.content.model.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author hyb
 * @version 1.0
 * @description 保存课程计划dto，包括新增、修改。如果传递了课程计划id说明当前是要修改该课程计划，否则是新增一个课程计划。
 * @date 2024/11/13
 */
@Data
public class SaveTeachplanDto {
    /***
     * 教学计划id
     */
    private Long id;

    /**
     * 课程计划名称
     */
    @NotEmpty(message = "课程计划名称不能为空")
    private String pname;

    /**
     * 课程计划父级Id
     */
    @NotNull(message = "课程计划父级Id不能为null")
    private Long parentid;

    /**
     * 层级，分为1、2、3级
     */
    @NotNull(message = "课程计划层级不能为null")
    private Integer grade;

    /**
     * 课程类型:1视频、2文档
     */
    private String mediaType;


    /**
     * 课程标识
     */
    @NotNull(message = "课程标识不能为null")
    private Long courseId;

    /**
     * 课程发布标识
     */
    private Long coursePubId;


    /**
     * 是否支持试学或预览（试看）
     */
    private String isPreview;

}
