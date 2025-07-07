package com.xuecheng.media.api;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.po.MediaFiles;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hyb
 * @version 1.0
 * @description 媒资文件管理接口
 * @date 2024/12/20
 */

@Api(value = "媒资文件管理接口",tags = "媒资文件管理接口")
@RestController
@RequestMapping("/open")
public class MediaOpenController {

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @ApiOperation("预览文件")
    @GetMapping("/preview/{mediaId}")
    public RestResponse<String> getPlayUrlByMediaId(@PathVariable("mediaId") String mediaId){
        MediaFiles mediaFiles = mediaFilesMapper.selectById(mediaId);
        if(mediaFiles == null){
            XueChengPlusException.cast("找不到视频");
        }
        String url = mediaFiles.getUrl();
        if(StringUtils.isEmpty(url)){
            XueChengPlusException.cast("视频还没有转码处理");
        }
        return RestResponse.success(url);
    }
}
