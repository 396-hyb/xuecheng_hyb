package com.xuecheng.checkcode.controller;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.checkcode.model.CheckCodeParamsDto;
import com.xuecheng.checkcode.model.CheckCodeResultDto;
import com.xuecheng.checkcode.service.CheckCodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.regex.Pattern;

/**
 * @author Mr.M
 * @version 1.0
 * @description 验证码服务接口
 * @date 2022/9/29 18:39
 */
@Api(value = "验证码服务接口")
@RestController
public class CheckCodeController {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$";

    @Resource(name = "PicCheckCodeService")
    private CheckCodeService picCheckCodeService;

    @Resource(name = "PhoneAndEmailCheckCodeService")
    private CheckCodeService phoneAndEmailCheckCodeService;

    @ApiOperation(value="生成pic验证信息", notes="生成pic验证信息")
    @PostMapping(value = "/pic")
    public CheckCodeResultDto generatePicCheckCode(CheckCodeParamsDto checkCodeParamsDto){
        return picCheckCodeService.generate(checkCodeParamsDto);
    }

    @ApiOperation(value="生成手机或者邮箱验证信息", notes="生成手机或者邮箱验证信息")
    @PostMapping(value = "/phone")
    public CheckCodeResultDto generatePhoneAndEmailCheckCode(@RequestParam("param1") String param){
        String checkCodeType = null;
        if (Pattern.matches(PHONE_REGEX, param)) {
            checkCodeType = "sms";
        } else if (Pattern.matches(EMAIL_REGEX, param)) {
            checkCodeType = "email";
        } else {
            throw new XueChengPlusException("无效的手机号或邮箱地址");
        }
        CheckCodeParamsDto checkCodeParamsDto = new CheckCodeParamsDto();
        checkCodeParamsDto.setCheckCodeType(checkCodeType);
        checkCodeParamsDto.setParam1(param);
        return phoneAndEmailCheckCodeService.generate(checkCodeParamsDto);
    }


    @ApiOperation(value="校验", notes="校验")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "业务名称", required = true, dataType = "String", paramType="query"),
            @ApiImplicitParam(name = "key", value = "验证key", required = true, dataType = "String", paramType="query"),
            @ApiImplicitParam(name = "code", value = "验证码", required = true, dataType = "String", paramType="query")
    })
    @PostMapping(value = "/verify")
    public Boolean verify(String key, String code){
        Boolean isSuccess = picCheckCodeService.verify(key,code);
        return isSuccess;
    }

}
