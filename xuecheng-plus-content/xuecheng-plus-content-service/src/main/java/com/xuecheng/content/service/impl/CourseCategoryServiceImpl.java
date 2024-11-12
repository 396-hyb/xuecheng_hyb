package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author hyb
 * @version 1.0
 * @description 课程分类业务接口实现
 * @date 2024/11/12
 */
@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {

    @Autowired
    public CourseCategoryMapper courseCategoryMapper;

    /**
     * 课程分类树形结构查询
     * @param id
     * @return
     */
    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {

        List<CourseCategoryTreeDto> categoryTreeDtos = courseCategoryMapper.selectTreeNodes(id);

        //将list转map,以备使用,排除根节点
        Map<String, CourseCategoryTreeDto> treeDtoMap = categoryTreeDtos.stream()
                .filter(items -> !id.equals(items.getId()))
                .collect(Collectors.toMap(key -> key.getId(), value -> value, (key1, key2) -> key2));

        //最终返回的list
        List<CourseCategoryTreeDto> queryCategoryTreeDtos = new ArrayList<>();

        //依次遍历每个元素, 插入最终返回的list
        categoryTreeDtos.stream()
                .filter(items -> !id.equals(items.getId()))
                .forEach(items -> {
                    if(items.getParentid().equals(id)){
                        queryCategoryTreeDtos.add(items);
                    }
                    //找到当前节点的父节点
                    CourseCategoryTreeDto treeDtoParent = treeDtoMap.get(items.getParentid());

                    if (treeDtoParent != null) {
                        if (treeDtoParent.getChildrenTreeNodes() == null) {
                            treeDtoParent.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
                        }
                        treeDtoParent.getChildrenTreeNodes().add(items);
                    }
                });

        return queryCategoryTreeDtos;
    }
}
