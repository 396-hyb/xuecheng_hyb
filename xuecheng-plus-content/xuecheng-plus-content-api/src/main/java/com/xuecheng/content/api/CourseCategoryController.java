package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CourseCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author hyb
 * @version 1.0
 * @description 课程分类树型编辑接口
 * @date 2024/11/12
 */
@Api(value = "课程分类树型信息编辑接口")
@Slf4j
@RestController
public class CourseCategoryController {

    @Autowired
    public CourseCategoryService courseCategoryService;

    @Autowired
    public CourseBaseInfoService courseBaseInfoService;

    //查询树型分类课程信息
    @ApiOperation("课程分类树形结构查询")
    @GetMapping("/course-category/tree-nodes")
    public List<CourseCategoryTreeDto> queryTreeNodes(){
        return courseCategoryService.queryTreeNodes("1");
    }

}
