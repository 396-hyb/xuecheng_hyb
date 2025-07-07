package com.xuecheng.auth.controller;

import com.xuecheng.ucenter.model.dto.RegisterParamsDto;
import com.xuecheng.ucenter.service.RegisterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hyb
 * @version 1.0
 * @description 注册用户
 * @date 2025/1/17
 */
@Slf4j
@RestController
public class RegisterController {

    @Autowired
    RegisterService registerService;

    @PostMapping("/register")
    public void register(@RequestBody RegisterParamsDto registerParamsDto){

        registerService.register(registerParamsDto);

    }
}
