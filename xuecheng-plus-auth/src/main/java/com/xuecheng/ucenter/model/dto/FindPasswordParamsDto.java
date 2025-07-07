package com.xuecheng.ucenter.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * @author hyb
 * @version 1.0
 * @description 找回密码传入参数
 * @date 2025/1/13
 */

@Data
@ToString
public class FindPasswordParamsDto {

    @ApiModelProperty(value = "用户手机号")
    private String cellphone;

    @ApiModelProperty(value = "验证码")
    private String checkcode;

    @ApiModelProperty(value = "验证码在redis中的key")
    private String checkcodekey;

    @ApiModelProperty(value = "确认密码")
    private String confirmpwd;

    @ApiModelProperty(value = "用户邮件")
    private String email;

    @ApiModelProperty(value = "密码")
    private String password;

}
