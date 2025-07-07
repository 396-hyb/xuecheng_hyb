package com.xuecheng.auth.controller;

import com.xuecheng.ucenter.model.dto.FindPasswordParamsDto;
import com.xuecheng.ucenter.service.PasswordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hyb
 * @version 1.0
 * @description 密码服务
 * @date 2025/1/16
 */

@Slf4j
@RestController
public class PasswordController {

    @Autowired
    PasswordService passwordService;

    @PostMapping("/findpassword")
    public void findPassword(@RequestBody FindPasswordParamsDto findPasswordParamsDto){
        System.out.println();
        passwordService.findpassword(findPasswordParamsDto);
    }
}
