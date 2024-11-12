package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
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
        if(StringUtils.isBlank(addCourseDto.getName())){
            throw new XueChengPlusException("课程名称不能为空");
        }
        if (StringUtils.isBlank(addCourseDto.getMt())) {
            throw new XueChengPlusException("课程分类为空");
        }
        if (StringUtils.isBlank(addCourseDto.getSt())) {
            throw new XueChengPlusException("课程分类为空");
        }
        if (StringUtils.isBlank(addCourseDto.getGrade())) {
            throw new XueChengPlusException("课程等级为空");
        }
        if (StringUtils.isBlank(addCourseDto.getTeachmode())) {
            throw new XueChengPlusException("教育模式为空");
        }
        if (StringUtils.isBlank(addCourseDto.getUsers())) {
            throw new XueChengPlusException("适应人群为空");
        }
        if (StringUtils.isBlank(addCourseDto.getCharge())) {
            throw new XueChengPlusException("收费规则为空");
        }

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
            courseMarketObj.setId(courseMarketNew.getId());
            return courseMarketMapper.updateById(courseMarketObj);
        }
    }

    //查询课程基本信息及营销信息
    private CourseBaseInfoDto getCourseBaseInfo(Long courseId) {
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
}
