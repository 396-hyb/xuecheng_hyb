package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author hyb
 * @version 1.0
 * @description 课程计划信息管理业务接口实现
 * @date 2024/11/13
 */
@Service
public class TeachplanServiceImpl implements TeachplanService {

    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;

    @Override
    public List<TeachplanDto> selectTreeNodes(Long courseId) {
        return teachplanMapper.selectTreeNodes(courseId);
    }

    @Transactional
    @Override
    public void saveTeachplan(SaveTeachplanDto teachplanDto) {
        Long id = teachplanDto.getId();
        Long courseId = teachplanDto.getCourseId();
        Long parentid = teachplanDto.getParentid();

        if(id == null){
            //新增
            //设置排序号
            int orderBy = this.getTeachplanCount(courseId, parentid);
            Teachplan teachplan = new TeachplanDto();
            BeanUtils.copyProperties(teachplanDto,teachplan);
            teachplan.setOrderby(orderBy + 1);
            teachplanMapper.insert(teachplan);
        }else{
            //修改
            Teachplan teachplan = teachplanMapper.selectById(id);
            BeanUtils.copyProperties(teachplanDto,teachplan);
            teachplanMapper.updateById(teachplan);
        }
    }

    @Transactional
    @Override
    public void deleteTeachplan(Long id) {
        //查询该课程计划
        Teachplan teachplan = teachplanMapper.selectById(id);

        if(teachplan == null){
            XueChengPlusException.cast("该课程计划已被删除");
        }

        Long parentid = teachplan.getParentid();
        Long courseId = teachplan.getCourseId();
        if(parentid == 0){ //删除第一级别
            //判断是否还有附属课程计划
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Teachplan::getParentid,id).eq(Teachplan::getCourseId,courseId);
            Integer i = teachplanMapper.selectCount(queryWrapper);
            if(i > 0){
                XueChengPlusException.cast("该课程计划不能删除，因为还有附属课程");
            }
            //删除该课程计划
            int delete = teachplanMapper.deleteById(id);
            if(delete < 1){
                XueChengPlusException.cast("该课程计划删除失败");
            }
        }else{ //删除第二级别
            //判断是否有关联媒体文件
            LambdaQueryWrapper<TeachplanMedia> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TeachplanMedia::getTeachplanId,id);
            Integer mediaCount = teachplanMediaMapper.selectCount(queryWrapper);
            if(mediaCount > 0){
                //删除对应的媒体文件
                int deleteMedia = teachplanMediaMapper.delete(queryWrapper);
                if(deleteMedia != mediaCount){
                    XueChengPlusException.cast("该课程计划的媒体文件删除失败");
                }
            }
            //删除课程计划
            int deleteTeachplan = teachplanMapper.deleteById(id);
            if(deleteTeachplan < 1){
                XueChengPlusException.cast("该课程计划删除失败");
            }
        }
    }

    @Transactional
    @Override
    public void movedownTeachplan(Long id) {
        //查询该课程计划
        Teachplan teachplan = teachplanMapper.selectById(id);

        Long parentid = teachplan.getParentid();
        Long courseId = teachplan.getCourseId();
        Integer orderby = teachplan.getOrderby();

        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getParentid,parentid)
                .eq(Teachplan::getCourseId,courseId)
                .gt(Teachplan::getOrderby,orderby)
                .orderByAsc(Teachplan::getOrderby)
                .last("limit 1");
        Teachplan teachplanDown = teachplanMapper.selectOne(queryWrapper);  // 如果没有记录满足条件，selectOne 方法将返回 null。
        if(teachplanDown == null){
            XueChengPlusException.cast("无法再下移");
        }
        //设置orderby字段
        teachplan.setOrderby(teachplanDown.getOrderby());
        teachplanDown.setOrderby(orderby);
        //更新
        int i1 = teachplanMapper.updateById(teachplan);
        if(i1 < 1){
            XueChengPlusException.cast("下移失败");
        }
        int i2 = teachplanMapper.updateById(teachplanDown);
        if(i2 < 1){
            XueChengPlusException.cast("下移失败");
        }
    }

    @Override
    public void moveupTeachplan(Long id) {
        //查询该课程计划
        Teachplan teachplan = teachplanMapper.selectById(id);

        Long parentid = teachplan.getParentid();
        Long courseId = teachplan.getCourseId();
        Integer orderby = teachplan.getOrderby();

        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getParentid,parentid)
                .eq(Teachplan::getCourseId,courseId)
                .lt(Teachplan::getOrderby,orderby)
                .orderByDesc(Teachplan::getOrderby)
                .last("limit 1");
        Teachplan teachplanUp = teachplanMapper.selectOne(queryWrapper);  // 如果没有记录满足条件，selectOne 方法将返回 null。
        if(teachplanUp == null){
            XueChengPlusException.cast("无法再上移");
        }
        //设置orderby字段
        teachplan.setOrderby(teachplanUp.getOrderby());
        teachplanUp.setOrderby(orderby);
        //更新
        int i1 = teachplanMapper.updateById(teachplan);
        if(i1 < 1){
            XueChengPlusException.cast("上移失败");
        }
        int i2 = teachplanMapper.updateById(teachplanUp);
        if(i2 < 1){
            XueChengPlusException.cast("上移失败");
        }
    }


    /**
     * 获取最新的排序号
     * @param courseId
     * @param parentId
     * @return
     */
    private int getTeachplanCount(long courseId,long parentId){
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId,courseId)
                        .eq(Teachplan::getParentid,parentId)
                                .orderByDesc(Teachplan::getOrderby)
                                        .last("limit 1");
        Teachplan teachplan = teachplanMapper.selectOne(queryWrapper);
        if(teachplan == null){
            return 0;
        }
        return teachplan.getOrderby();
    }

}
