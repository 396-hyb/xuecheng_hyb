package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author hyb
 * @version 1.0
 * @description 课程计划树型结构dto
 * @date 2024/11/13
 */
@Data
@ApiModel(value="TeachplanDto", description="课程计划树型结构dto")
public class TeachplanDto extends Teachplan {

    //课程计划关联的媒资信息
    @ApiModelProperty(value = "课程媒资信息")
    TeachplanMedia teachplanMedia;

    //子结点
    @ApiModelProperty(value = "子结点")
    List<TeachplanDto> teachPlanTreeNodes;
}
