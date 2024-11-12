package com.xuecheng.content;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author hyb
 * @version 1.0
 * @description 基本课程mapper测试
 * @date 2024/11/11
 */
@SpringBootTest
public class CourseBaseMapperTests {
    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Test
    void testCourseBaseMapper(){

//        CourseBase courseBase = courseBaseMapper.selectById(80L);
//        Assertions.assertNotNull(courseBase);

        //测试查询接口,分页查询
        LambdaQueryWrapper<CourseBase> wrapper = new LambdaQueryWrapper<>();
        //查询条件
        QueryCourseParamsDto courseParamsDto = new QueryCourseParamsDto();
        courseParamsDto.setCourseName("java");
//        courseParamsDto.setAuditStatus("202004");
//        courseParamsDto.setPublishStatus("203001");

        //拼接查询条件
        //根据课程名称模糊查询  name like '%名称%'
        wrapper.like(StringUtils.isNotEmpty(courseParamsDto.getCourseName()),CourseBase::getName,courseParamsDto.getCourseName());
        //根据课程审核状态
        wrapper.eq(StringUtils.isNotEmpty(courseParamsDto.getAuditStatus()),CourseBase::getAuditStatus,courseParamsDto.getAuditStatus());

        //分页参数
        PageParams pageParams = new PageParams();
        pageParams.setPageNo(1L);
        pageParams.setPageSize(10L);
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(),pageParams.getPageSize());

        //分页查询
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
        System.out.println(pageResult);

    }
}
