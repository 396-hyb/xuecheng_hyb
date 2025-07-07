package com.xuecheng.content;

import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * @author hyb
 * @version 1.0
 * @description 测试使用feign远程上传文件
 * @date 2024/12/23
 */

@SpringBootTest
public class FeignUploadTest {

    @Autowired
    MediaServiceClient mediaServiceClient;

    @Test
    public void test(){
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(new File("F:\\Java learning\\video\\2.html"));
        mediaServiceClient.upload(multipartFile,"course/2.html");
    }
}
