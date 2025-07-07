package com.xuecheng.ucenter.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * @author hyb
 * @version 1.0
 * @description 注册用户传入参数
 * @date 2025/1/17
 */
@Data
@ToString
public class RegisterParamsDto {

    @ApiModelProperty(value = "用户手机号")
    private String cellphone;

    @ApiModelProperty(value = "用户账号")
    private String username;

    @ApiModelProperty(value = "用户邮件")
    private String email;

    @ApiModelProperty(value = "用户昵称")
    private String nickname;

    @ApiModelProperty(value = "密码")
    private String password;

    @ApiModelProperty(value = "确认密码")
    private String confirmpwd;

    @ApiModelProperty(value = "验证码在redis中的key")
    private String checkcodekey;

    @ApiModelProperty(value = "验证码")
    private String checkcode;


}
