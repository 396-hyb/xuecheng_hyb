package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * @author hyb
 * @version 1.0
 * @description 课程查询参数DTO
 * @date 2024/11/9
 */

@Data
@ToString
public class QueryCourseParamsDto {
    //审核状态
    @ApiModelProperty(value = "审核状态")
    private String auditStatus;

    //课程名称
    @ApiModelProperty(value = "课程名称")
    private String courseName;

    //发布状态
    @ApiModelProperty(value = "发布状态")
    private String publishStatus;
}
