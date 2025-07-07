package com.xuecheng.ucenter.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author hyb
 * @version 1.0
 * @description 远程调用验证码服务
 * @date 2025/1/11
 */
@FeignClient(value = "checkcode", fallbackFactory = CheckCodeClientFallbackFactory.class)
@RequestMapping("/checkcode")
public interface CheckCodeClient {

    @PostMapping(value = "/verify")
    public Boolean verify(@RequestParam("key") String key, @RequestParam("code") String code);
}
