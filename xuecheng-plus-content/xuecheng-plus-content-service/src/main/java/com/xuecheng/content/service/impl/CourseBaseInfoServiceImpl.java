package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.*;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseBaseInfoService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author hyb
 * @version 1.0
 * @description 课程信息管理业务接口实现类
 * @date 2024/11/11
 */

@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Autowired
    public CourseBaseMapper courseBaseMapper;

    @Autowired
    public CourseMarketMapper courseMarketMapper;

    @Autowired
    public CourseCategoryMapper courseCategoryMapper;

    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    CourseTeacherMapper courseTeacherMapper;

    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {


        //分页查询
        LambdaQueryWrapper<CourseBase> wrapper = new LambdaQueryWrapper<>();
        //查询条件 courseParamsDto

        //拼接查询条件
        //根据课程名称模糊查询  name like '%名称%'
        wrapper.like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()),CourseBase::getName,queryCourseParamsDto.getCourseName());
        //根据课程审核状态
        wrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()),CourseBase::getAuditStatus,queryCourseParamsDto.getAuditStatus());
        //根据课程发布状态
        wrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()),CourseBase::getStatus,queryCourseParamsDto.getPublishStatus());

        //分页参数
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(),pageParams.getPageSize());

        //分页查询 pageParams
        Page<CourseBase> iPage = courseBaseMapper.selectPage(page, wrapper);

        //数据
        List<CourseBase> items = iPage.getRecords();
        //总记录数
        long total = iPage.getTotal();

        //准备返回数据
        PageResult<CourseBase> pageResult = new PageResult<>();
        pageResult.setItems(items);
        pageResult.setCounts(total);
        pageResult.setPage(pageParams.getPageNo());
        pageResult.setPageSize(pageParams.getPageSize());
        return pageResult;
    }

    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto) {
        //合法性校验
        //controller已经开启 @validate,这里不做

        //将 AddCourseDto 赋给 CourseBase
        CourseBase courseBaseNew = new CourseBase();
        BeanUtils.copyProperties(addCourseDto, courseBaseNew);
        //补充 CourseBase 不能从 AddCourseDto 获得的信息
        courseBaseNew.setCompanyId(companyId);
        courseBaseNew.setCreateDate(LocalDateTime.now());
        courseBaseNew.setAuditStatus("202002");
        courseBaseNew.setStatus("203001");

        //插入课程基本信息表
        int insert = courseBaseMapper.insert(courseBaseNew);
        if(insert <= 0){
            throw new XueChengPlusException("新增课程基本信息失败");
        }

        //向课程营销表保存课程营销信息
        //将 AddCourseDto 赋给 CourseMarket
        CourseMarket courseMarketNew = new CourseMarket();
        Long courseId = courseBaseNew.getId();
        BeanUtils.copyProperties(addCourseDto, courseMarketNew);
        //补充 CourseMarket 不能从 AddCourseDto 获得的信息, 即课程id
        courseMarketNew.setId(courseId);
        int i = saveCourseMarket(courseMarketNew);
        if(i<=0){
            throw new XueChengPlusException("保存课程营销信息失败");
        }

        //查询课程基本信息及营销信息，并返回
        return getCourseBaseInfo(courseId);

    }



    //保存课程营销信息
    @Transactional
    private int saveCourseMarket(CourseMarket courseMarketNew) {
        //收费规则
        String charge = courseMarketNew.getCharge();

        if(StringUtils.isBlank(charge)){
            throw new XueChengPlusException("收费规则没有选择");
        }
        //收费规则为收费
        if(charge.equals("201001")){
            if(courseMarketNew.getPrice() == null || courseMarketNew.getPrice().floatValue() <= 0){
                throw new XueChengPlusException("课程为收费价格不能为空且必须大于0");
            }
        }
        //根据id从课程营销表查询，判读是插入还是更新
        CourseMarket courseMarketObj = courseMarketMapper.selectById(courseMarketNew.getId());
        if(courseMarketObj == null){
            return courseMarketMapper.insert(courseMarketNew);
        }else{
            BeanUtils.copyProperties(courseMarketNew, courseMarketObj);
            //为啥要拷贝，因为MP的updateById默认忽视更新null或空值，用户修改时可能会让某些字段为null或空值
            //courseMarketObj.setId(courseMarketNew.getId());
            // 感觉上面这句根本没用
            return courseMarketMapper.updateById(courseMarketObj);
        }
    }

    //查询课程基本信息及营销信息
    @Override
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        // 如果没有课程基本信息，则就没有CourseBaseInfoDto
        if(courseBase == null)
            return null;
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        // 可以没有营销信息
        if(courseMarket != null){
            BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);
        }
        //CourseBaseInfoDto中缺少大分类名称和小分类名称
        CourseCategory stName = courseCategoryMapper.selectById(courseBase.getSt());
        CourseCategory mtName = courseCategoryMapper.selectById((courseBase.getMt()));
        courseBaseInfoDto.setStName(stName.getName());
        courseBaseInfoDto.setStName(mtName.getName());

        return courseBaseInfoDto;
    }

    // 修改课程信息
    @Transactional
    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto) {
        Long courseId = editCourseDto.getId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase == null){
            XueChengPlusException.cast("该课程在数据库不存在");
        }
        //校验本机构只能修改本机构的课程
        if(!courseBase.getCompanyId().equals(companyId)){
            XueChengPlusException.cast("本机构只能修改本机构的课程");
        }

        //封装基本信息的数据
        BeanUtils.copyProperties(editCourseDto,courseBase);
        courseBase.setChangeDate(LocalDateTime.now());

        //更新课程基本信息
        int i = courseBaseMapper.updateById(courseBase);
        if(i <= 0){
            throw new XueChengPlusException("跟新课程基本信息失败");
        }

        //封装营销信息的数据
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(editCourseDto,courseMarket);
        this.saveCourseMarket(courseMarket);

        //查询课程信息
        return this.getCourseBaseInfo(courseId);
    }


    // 删除课程信息
    @Transactional
    @Override
    public void deleteCourseBase(Long companyId, Long id) {
        CourseBase courseBase = courseBaseMapper.selectById(id);

        String auditStatus = courseBase.getAuditStatus();
        if(!auditStatus.equals("202002")){
            XueChengPlusException.cast("课程的审核状态不是未提交，不能删除");
        }

        if(courseBase == null){
            XueChengPlusException.cast("该课程在数据库不存在");
        }

        //校验本机构只能修改本机构的课程
        if(!courseBase.getCompanyId().equals(companyId)){
            XueChengPlusException.cast("本机构只能修改本机构的课程");
        }

        // 删除课程基本信息
        int i = courseBaseMapper.deleteById(id);
        if(i < 1){
            XueChengPlusException.cast("删除失败，课程基本信息没能删除");
        }
        // 删除营销信息
        int delete = courseMarketMapper.deleteById(id);
        if(delete < 1){
            XueChengPlusException.cast("删除失败，课程营销信息没能删除");
        }
        // 删除课程计划
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId,id);
        Integer count = teachplanMapper.selectCount(queryWrapper);
        int deleteTeachplan = teachplanMapper.delete(queryWrapper);
        if(!count.equals(deleteTeachplan)){
            XueChengPlusException.cast("删除失败，有课程计划未删除");
        }
        //删除媒体信息
        LambdaQueryWrapper<TeachplanMedia> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(TeachplanMedia::getCourseId, id);
        Integer countTeachplanMedia = teachplanMediaMapper.selectCount(queryWrapper1);
        int deleteTeachplanMedia = teachplanMediaMapper.delete(queryWrapper1);
        if(!countTeachplanMedia.equals(deleteTeachplanMedia)){
            XueChengPlusException.cast("删除失败，有课程计划对于媒体信息未删除");
        }
        // 删除课程教师信息
        LambdaQueryWrapper<CourseTeacher> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.eq(CourseTeacher::getCourseId, id);
        Integer countCourseTeacher = courseTeacherMapper.selectCount(queryWrapper2);
        int deleteCourseTeacher = courseTeacherMapper.delete(queryWrapper2);
        if(!countCourseTeacher.equals(deleteCourseTeacher)){
            XueChengPlusException.cast("删除失败，有课程老师未删除");
        }
    }
}
