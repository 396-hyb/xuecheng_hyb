package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;

import java.util.List;

/**
 * @author hyb
 * @version 1.0
 * @description 课程计划service接口实现类
 * @date 2024/11/13
 */
public interface TeachplanService {

    /**
     * 查询课程计划树型结构
     * @param courseId
     * @return
     */
    List<TeachplanDto> selectTreeNodes(Long courseId);

    /**
     * 课程计划创建或修改
     * @param saveTeachplanDto
     */
    void saveTeachplan(SaveTeachplanDto saveTeachplanDto);

    /**
     * 删除课程计划
     * @param id
     */
    void deleteTeachplan(Long id);

    /**
     * 向下移动课程计划
     * @param teachplanId
     */
    void movedownTeachplan(Long teachplanId);

    /**
     * 向上移动课程计划
     * @param teachplanId
     */
    void moveupTeachplan(Long teachplanId);
}
