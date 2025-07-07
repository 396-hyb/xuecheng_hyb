package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.TeachplanMedia;

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

    /**
     * 教学计划绑定媒资
     * @param bindTeachplanMediaDto
     * @return
     */
    public TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);

    /**
     * 解除教学计划绑定的媒资
     * @param teachplanId
     * @param mediaId
     */
    void deleteAssociationMedia(Long teachplanId, String mediaId);
}
