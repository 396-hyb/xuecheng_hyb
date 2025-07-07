package com.xuecheng.content.feignclient;

import com.xuecheng.content.model.dto.CourseIndex;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author hyb
 * @version 1.0
 * @description 搜索服务远程接口
 * @date 2024/12/24
 */
@FeignClient(value = "search",fallbackFactory = SearchServiceClientFallbackFactory.class)
public interface SearchServiceClient {
    @PostMapping("/search/index/course")
    public Boolean add(@RequestBody CourseIndex courseIndex);
}
