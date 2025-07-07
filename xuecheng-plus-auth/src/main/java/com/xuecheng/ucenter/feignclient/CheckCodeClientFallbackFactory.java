package com.xuecheng.ucenter.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author hyb
 * @version 1.0
 * @description 调用验证码服务降级逻辑
 * @date 2025/1/11
 */
@Slf4j
@Component
public class CheckCodeClientFallbackFactory implements FallbackFactory<CheckCodeClient> {
    @Override
    public CheckCodeClient create(Throwable throwable) {
        return new CheckCodeClient() {
            @Override
            public Boolean verify(String key, String code) {
                //降级方法
                log.debug("调用验证码服务时发生熔断，异常信息:{}",throwable.toString(),throwable);
                return null;
            }
        };
    }
}
