package com.xuecheng.content.feignclient;

import com.xuecheng.content.model.dto.CourseIndex;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author hyb
 * @version 1.0
 * @description TODO
 * @date 2024/12/24
 */
@Slf4j
@Component
public class SearchServiceClientFallbackFactory implements FallbackFactory<SearchServiceClient> {

    @Override
    public SearchServiceClient create(Throwable throwable) {
        return new SearchServiceClient() {
            @Override
            public Boolean add(CourseIndex courseIndex) {
                //降级方法
                log.debug("调用课程搜索服务远程接口发生熔断，异常信息:{}",throwable.toString(),throwable);
                return false;
            }
        };
    }
}
