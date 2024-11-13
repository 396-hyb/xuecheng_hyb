package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author hyb
 * @version 1.0
 * @description 添加课程dto
 * @date 2024/11/13
 */
@Data
@ApiModel(value="EditCourseDto", description="修改课程基本信息")
public class EditCourseDto extends AddCourseDto{

    @NotNull(message = "课程id不能为空")
    @ApiModelProperty(value = "课程id", required = true)
    private Long id;
}
