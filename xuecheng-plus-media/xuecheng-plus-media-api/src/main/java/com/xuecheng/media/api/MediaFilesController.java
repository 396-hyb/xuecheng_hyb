package com.xuecheng.media.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @author hyb
 * @version 1.0
 * @description 媒资编辑接口
 * @date 2024/11/23
 */
@RestController
@Slf4j
@Api(value = "媒资编辑接口", tags = "媒资编辑")
public class MediaFilesController {

    @Autowired
    MediaFileService mediaFileService;


    @ApiOperation("媒资列表查询接口")
    @PostMapping("/files")
    public PageResult<MediaFiles> list(PageParams pageParams, @RequestBody QueryMediaParamsDto queryMediaParamsDto){
        //TODO:companyId
        Long companyId = 1232141425L;
        return mediaFileService.queryMediaFiels(companyId,pageParams,queryMediaParamsDto);

    }


    @ApiOperation(value = "上传文件")
    @RequestMapping(value = "/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public UploadFileResultDto upload(
            @RequestPart("filedata") MultipartFile filedata,
//            @RequestParam(value = "folder", required = false) String folder,
            @RequestParam(value = "objectName", required = false) String objectName
    ) throws IOException {

        //TODO:companyId
        Long companyId = 1232141425L;

        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        uploadFileParamsDto.setFilename(filedata.getOriginalFilename());
        uploadFileParamsDto.setFileType("001001");
        uploadFileParamsDto.setFileSize(filedata.getSize());

        //创建临时文件
        File tempFile = File.createTempFile("minio", ".temp");
        //上传的文件拷贝到临时文件
        filedata.transferTo(tempFile);
        //文件路径
        String localFilePath = tempFile.getAbsolutePath();
        //上传文件
        return mediaFileService.uploadFile(companyId, uploadFileParamsDto, localFilePath, objectName);
    }




}
