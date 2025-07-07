package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MinioClient minioClient;

    @Autowired
    MediaFileService currentProxy;

    @Autowired
    MediaProcessMapper mediaProcessMapper;

    //存储普通文件
    @Value("${minio.bucket.files}")
    private String bucket_mediafiles;

    //存储视频
    @Value("${minio.bucket.videofiles}")
    private String bucket_videoFiles;

    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }

    //根据扩展名获取mimeType
    @Override
    public String getMimeType(String extension){
        if(extension == null){
            extension = "";
        }
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if(extensionMatch != null){
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }


    //将文件上传到minio
    @Override
    public boolean addMediaFilesToMinIO(String localFilePath,String mimeType,String bucket, String objectName){
        try {
            //上传文件的参数信息
            UploadObjectArgs objectArgs = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .filename(localFilePath)
                    .contentType(mimeType)
                    .build();
            //上传文件
            minioClient.uploadObject(objectArgs);
            log.debug("上传文件到minio成功,bucket:{},objectName:{}",bucket,objectName);
            System.out.println("上传成功");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件到minio出错,bucket:{},objectName:{},错误原因:{}",bucket,objectName,e.getMessage(),e);
            XueChengPlusException.cast("上传失败");
        }
        return false;
    }

    //获取文件默认存储目录路径 年/月/日
    private String getDefaultFolderPath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String folder = sdf.format(new Date()).replace("-", "/") + "/";
        return folder;
    }
    //获取文件的md5
    private String getFileMd5(File file){
        String md5Hex = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            md5Hex = DigestUtils.md5Hex(fileInputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return md5Hex;
    }


    //上传文件
    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath, String objectName) {

        File file = new File(localFilePath);
        if(!file.exists()){
            XueChengPlusException.cast("文件不存在");
        }

        //文件名
        String filename = uploadFileParamsDto.getFilename();

        //先得到扩展名
        String extension = filename.substring(filename.lastIndexOf("."));

        //得到mimeType
        String mimeType = getMimeType(extension);

        //文件目录
        String folder = getDefaultFolderPath();

        //文件的md5值
        String fileMd5 = getFileMd5(new File(localFilePath));

        //存储到minio中的对象名(带目录)
        if(StringUtils.isEmpty(objectName)){
            objectName =  folder + fileMd5 + extension;
        }

        //上传文件到minio
        boolean addMediaFilesToMinIO = addMediaFilesToMinIO(localFilePath, mimeType, bucket_mediafiles, objectName);
        if(!addMediaFilesToMinIO){
            XueChengPlusException.cast("上传文件到Minio失败");
        }

        //入库文件信息
        uploadFileParamsDto.setFileSize(file.length());
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_mediafiles, objectName);
        if(mediaFiles == null){
            XueChengPlusException.cast("文件上传后保存文件信息到数据库失败");
        }

        //准备返回的对象
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
        return uploadFileResultDto;
    }


    /**
     * @description 将文件信息添加到文件表
     * @param companyId  机构id
     * @param fileMd5  文件md5值
     * @param uploadFileParamsDto  上传文件的信息
     * @param bucket  桶
     * @param objectName 对象名称
     * @return com.xuecheng.media.model.po.MediaFiles
     * @author Mr.M
     * @date 2022/10/12 21:22
     */
    @Transactional
    public MediaFiles addMediaFilesToDb(Long companyId,String fileMd5,UploadFileParamsDto uploadFileParamsDto,String bucket,String objectName){
        //将文件信息保存到数据库

        //先判断数据库是否已经存在该文件信息
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if(mediaFiles == null){
            mediaFiles = new MediaFiles();
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(fileMd5);
            mediaFiles.setFileId(fileMd5);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setBucket(bucket);
            mediaFiles.setFilePath(objectName);
            mediaFiles.setUrl("/"+bucket+"/"+objectName);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setAuditMind("002003");
            mediaFiles.setStatus("1");
            int insert = mediaFilesMapper.insert(mediaFiles);
            if(insert < 1){
                log.error("保存文件信息到数据库失败,{}",mediaFiles.toString());
                XueChengPlusException.cast("保存文件信息到数据库失败");
            }
            //添加到待处理任务表
            addWaitingTask(mediaFiles);
            log.debug("保存文件信息到数据库成功,{}",mediaFiles.toString());
        }
        return mediaFiles;

    }

    /**
     * 添加待处理任务
     * @param mediaFiles 媒资文件信息
     */
    private void addWaitingTask(MediaFiles mediaFiles) {
        //文件名称
        String filename = mediaFiles.getFilename();
        //文件扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        //mimetype
        String mimeType = getMimeType(extension);
        if(mimeType.equals("video/x-msvideo")){
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles,mediaProcess);
            mediaProcess.setStatus("1"); //未处理
            mediaProcess.setFailCount(0); //失败次数默认为0
            mediaProcessMapper.insert(mediaProcess);
        }

    }

    //判断文件是否存在
    @Override
    public RestResponse<Boolean> checkfile(String fileMd5) {
        //根据MD5查询媒资文件表
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);

        //判断minioClient是否存在对于文件
        if(mediaFiles != null){
            //文件流
            InputStream stream = null;
            try {
                GetObjectArgs getObjectArgs = GetObjectArgs
                        .builder()
                        .bucket(mediaFiles.getBucket())
                        .object(mediaFiles.getFilePath())
                        .build();
                stream = minioClient.getObject(getObjectArgs);
                if(stream != null){
                    stream.close();
                    //文件已存在
                    log.debug("文件已经存在");
                    System.out.println("----------------------文件已经存在----------------------");
                    return RestResponse.success(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //文件不存在
        return RestResponse.success(false);
    }

    //判断分块文件是否存在(video)
    @Override
    public RestResponse<Boolean> checkchunk(String fileMd5, int chunk) {
        //分块文件的目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //分块文件的路径
        String chunkFilePath = chunkFileFolderPath + Integer.toString(chunk);
        //文件流
        InputStream fileInputStream = null;

        try {
            GetObjectArgs getObjectArgs = GetObjectArgs
                    .builder()
                    .bucket(bucket_videoFiles)
                    .object(chunkFilePath)
                    .build();
            fileInputStream = minioClient.getObject(getObjectArgs);
            if(fileInputStream != null){
                //分块已存在
                fileInputStream.close();
                log.debug("分块已经存在");
                return RestResponse.success(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //分块不存在
        return RestResponse.success(false);
    }

    //上传分块文件
    @Override
    public RestResponse uploadchunk(String fileMd5, int chunk, String localChunkFilePath) {
        //分块文件的目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //分块文件的路径
        String chunkFilePath = chunkFileFolderPath + Integer.toString(chunk);
        String mimeType = getMimeType(null);
        boolean b = addMediaFilesToMinIO(localChunkFilePath, mimeType, bucket_videoFiles, chunkFilePath);
        if(!b){
            log.debug("上传分块文件失败:{},bucket:{},object:{}", chunkFilePath,bucket_videoFiles,chunkFilePath);
            return RestResponse.validfail(false, "上传分块文件失败");
        }
        log.debug("上传分块文件成功:{},bucket:{},object:{}", chunkFilePath,bucket_videoFiles,chunkFilePath);
        return RestResponse.success(true);
    }

    //合并文件块
    @Override
    public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        //获取分块文件路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        List<ComposeSource> sourceObjectList = Stream.iterate(0, i -> ++i)
                .limit(chunkTotal)
                .map(i -> ComposeSource.builder()
                        .bucket(bucket_videoFiles)
                        .object(chunkFileFolderPath.concat(Integer.toString(i)))
                        .build())
                .collect(Collectors.toList());
        //=====合并=====
        //文件名称
        String fileName = uploadFileParamsDto.getFilename();
        //文件扩展名
        String extName = fileName.substring(fileName.lastIndexOf("."));
        //合并文件路径
        String mergeFilePath = getFilePathByMd5(fileMd5, extName);
        try {
            ComposeObjectArgs composeObjectArgs = ComposeObjectArgs
                    .builder()
                    .bucket(bucket_videoFiles)
                    .object(mergeFilePath)
                    .sources(sourceObjectList)
                    .build();
            minioClient.composeObject(composeObjectArgs);
            System.out.println("文件合并成功");
        } catch (Exception e) {
            log.debug("文件合并异常,bucket:{},fileMd5:{},异常:{}",bucket_videoFiles,fileMd5,e.getMessage(),e);
            return RestResponse.validfail(false, "合并文件失败。");
        }

        // ====验证md5====
        //下载合并后的文件
        File downloadFile = this.downloadFileFromMinIO(bucket_videoFiles, mergeFilePath);
        if(downloadFile == null){
            log.debug("下载合并后文件失败,mergeFilePath:{}",mergeFilePath);
            return RestResponse.validfail(false, "下载合并后文件失败。");
        }
        //校验文件的完整性对文件的内容进行md5
        FileInputStream newFileInputStream = null;
        try {
            newFileInputStream = new FileInputStream(downloadFile);
            String md5Hex = DigestUtils.md5Hex(newFileInputStream);
            //比较md5值，不一致则说明文件不完整
            if(!fileMd5.equals(md5Hex)){
                return RestResponse.validfail(false, "文件合并校验失败，最终上传失败。");
            }
            //文件大小
            uploadFileParamsDto.setFileSize(downloadFile.length());

        } catch (Exception e) {
            log.debug("校验文件失败,fileMd5:{},异常:{}",fileMd5,e.getMessage(),e);
            return RestResponse.validfail(false, "文件合并校验失败，最终上传失败。");
        }finally {
            if(downloadFile != null){
                downloadFile.delete();
            }
        }

        //入库文件信息
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_videoFiles, mergeFilePath);
        if(mediaFiles == null){
            XueChengPlusException.cast("文件上传后保存文件信息到数据库失败");
        }
        //=====清除分块文件=====
        clearChunkFiles(bucket_videoFiles,chunkFileFolderPath,chunkTotal);
        return RestResponse.success(true);
    }

    public String getChunkFileFolderPath(String fileMd5){
        return fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" + "chunk/";
    }

    /**
     * 得到合并后的文件的地址
     * @param fileMd5 文件id即md5值
     * @param fileExt 文件扩展名
     * @return
     */
    private String getFilePathByMd5(String fileMd5,String fileExt){
        return fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }

    // 从minio下载文件
    @Override
    public File downloadFileFromMinIO(String bucket,String objectName){
        //临时文件
        File mininFile = null;
        FileOutputStream outputStream = null;
        try {
            GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket(bucket).object(objectName).build();
            InputStream stream = minioClient.getObject(getObjectArgs);
            //创建临时文件
            mininFile = File.createTempFile("minio","temp");
            outputStream = new FileOutputStream(mininFile);
            IOUtils.copy(stream, outputStream);
            return  mininFile;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(outputStream != null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    /**
     * 清除分块文件
     * @param chunkFileFolderPath 分块文件路径
     * @param chunkTotal 分块文件总数
     */
    private void clearChunkFiles(String bucket ,String chunkFileFolderPath,int chunkTotal){
        try {
            List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i)
                    .limit(chunkTotal)
                    .map(i -> new DeleteObject(chunkFileFolderPath.concat(Integer.toString(i))))
                    .collect(Collectors.toList());
            RemoveObjectsArgs removeObjectArgs = RemoveObjectsArgs.builder()
                    .bucket(bucket)
                    .objects(deleteObjects)
                    .build();
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectArgs);
            results.forEach(r -> {
                DeleteError deleteError = null;
                try {
                    deleteError = r.get();
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("删除分块文件失败,objectname:{}",deleteError.objectName(),e);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            log.error("删除分块文件失败,chunkFileFolderPath:{}",chunkFileFolderPath,e);
        }
    }
}
