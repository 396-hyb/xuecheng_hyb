package com.xuecheng.content;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author hyb
 * @version 1.0
 * @description TODO
 * @date 2024/11/12
 */
@SpringBootTest
public class CourseCategoryServiceTests {

    @Autowired
    public CourseCategoryService courseCategoryService;
    @Test
    void testCourseCategoryService(){
        List<CourseCategoryTreeDto> categoryTreeDtos = courseCategoryService.queryTreeNodes("1");
        System.out.println(categoryTreeDtos);
    }
}
