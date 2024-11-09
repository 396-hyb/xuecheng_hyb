package com.xuecheng.base.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


/**
 * @author hyb
 * @version 1.0
 * @description 分页查询通用参数
 * @date 2024/11/9
 */

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class PageParams {

    //当前页码
    @ApiModelProperty(value = "当前页码")
    public Long pageNO = 1L;

    //每页记录数默认值
    @ApiModelProperty(value = "每页记录数默认值")
    public Long pageSize = 10L;
}
