package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CourseTeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author hyb
 * @version 1.0
 * @description 课程老师编辑接口
 * @date 2024/11/18
 */

@Slf4j
@Api(value = "课程老师编辑接口",tags = "课程老师编辑接口")
@RestController
public class CourseTeacherController {

    @Autowired
    CourseTeacherService courseTeacherService;

    @ApiOperation("查询课程老师信息")
    @GetMapping("/courseTeacher/list/{courseId}")
    @ApiImplicitParam(value = "courseId", required = true, dataType = "Long", paramType = "path")
    public List<CourseTeacher> getCourseTeacher(@PathVariable Long courseId){
        List<CourseTeacher> courseTeacherList = courseTeacherService.selectCourseTeacher(courseId);
        return courseTeacherList;
    }

    @ApiOperation("课程老师创建或修改-根据课程老师id是否为null判断")
    @PostMapping("/courseTeacher")
    public CourseTeacher saveCourseTeacher(@RequestBody @Validated CourseTeacherDto courseTeacherDto){
        //TODO: 机构id，由于认证系统没有上线暂时硬编码
        Long companyId = 1232141425L;
        return courseTeacherService.saveCourseTeacher(companyId, courseTeacherDto);
    }

    @ApiOperation("删除课程老师")
    @DeleteMapping("/courseTeacher/course/{courseId}/{id}")
    public void deleteCourseTeacher(@PathVariable Long courseId, @PathVariable Long id){
        courseTeacherService.deleteCourseTeacher(courseId, id);
    }
}
