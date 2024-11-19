package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author hyb
 * @version 1.0
 * @description 课程计划编辑接口
 * @date 2024/11/13
 */

@Slf4j
@Api(value = "课程计划编辑接口",tags = "课程计划编辑接口")
@RestController
public class TeachplanController {

    @Autowired
    TeachplanService teachplanService;

    @ApiOperation("查询课程计划树形结构")
    @ApiImplicitParam(value = "courseId", required = true, dataType = "Long", paramType = "path")
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable Long courseId){
        return teachplanService.selectTreeNodes(courseId);
    }

    @ApiOperation("课程计划创建或修改-根据课程计划id是否为null判断")
    @PostMapping("/teachplan")
    public void saveTeachplan(@RequestBody SaveTeachplanDto saveTeachplanDto){
        teachplanService.saveTeachplan(saveTeachplanDto);
    }

    @ApiOperation("删除课程计划")
    @DeleteMapping("/teachplan/{teachplanId}")
    public void deleteTeachplan(@PathVariable Long teachplanId){
        teachplanService.deleteTeachplan(teachplanId);
    }

    @ApiOperation("下移课程计划")
    @PostMapping("/teachplan/movedown/{teachplanId}")
    public void movedownTeachplan(@PathVariable Long teachplanId){
        teachplanService.movedownTeachplan(teachplanId);
    }

    @ApiOperation("上移课程计划")
    @PostMapping("/teachplan/moveup/{teachplanId}")
    public void moveupTeachplan(@PathVariable Long teachplanId){
        teachplanService.moveupTeachplan(teachplanId);
    }

}
