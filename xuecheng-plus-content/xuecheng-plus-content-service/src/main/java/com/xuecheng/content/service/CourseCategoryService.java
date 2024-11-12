package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;

import java.util.List;

/**
 * @author hyb
 * @version 1.0
 * @description 课程分类业务接口
 * @date 2024/11/12
 */
public interface CourseCategoryService {

    /**
     * 课程分类树形结构查询
     * @param id
     * @return
     */
    public List<CourseCategoryTreeDto> queryTreeNodes(String id);
}
