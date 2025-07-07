package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import com.xuecheng.media.service.MediaFilesProcessService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author hyb
 * @version 1.0
 * @description 视频采用任务类
 * @date 2024/12/10
 */
@Slf4j
@Component
public class VideoTask {

    @Autowired
    MediaFileService mediaFileService;
    @Autowired
    MediaFilesProcessService mediaFileProcessService;


    @Value("${videoprocess.ffmpegpath}")
    String ffmpegpath;

    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws Exception{
        log.info("videoJobHandler开始执行.....");
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        List<MediaProcess> mediaProcessList = null;
        int size = 0;
        //mediaProcessList填充
        try {
            //取出cpu核心数作为一次处理数据的条数
            //一次处理视频数量不要超过cpu核心数
            int processors = Runtime.getRuntime().availableProcessors();
            mediaProcessList = mediaFileProcessService.getMediaProcessList(shardIndex, shardTotal, processors);
            size = mediaProcessList.size();
            log.debug("取出待处理视频任务{}条",size);
            if(size <= 0){
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        //启动size个线程的线程池
        ExecutorService threadPool = Executors.newFixedThreadPool(size);
        //计数器
        CountDownLatch countDownLatch = new CountDownLatch(size);
        //将处理任务加入线程池
        for(MediaProcess mediaProcess : mediaProcessList){
            threadPool.execute(() -> {
                        try {
                            //获取任务
                            Long taskId = mediaProcess.getId();
                            boolean b = mediaFileProcessService.startTask(taskId);
                            if(!b){
                                return;
                            }
                            log.debug("开始执行任务:{}", mediaProcess);
                            String bucket = mediaProcess.getBucket();
                            String filePath = mediaProcess.getFilePath();
                            String fileMd5 = mediaProcess.getFileId();
                            String filename = mediaProcess.getFilename();

                            //将要处理的文件下载到服务器上
                            File originalFile = mediaFileService.downloadFileFromMinIO(bucket, filePath);
                            if(originalFile == null){
                                log.debug("下载原文件失败:{}", bucket.concat(filePath));
                                mediaFileProcessService.saveProcessFinishStatus(taskId,"3",fileMd5,null,"下载待处理原文件失败");
                                return;
                            }
                            //处理下载的视频文件
                            //创建临时MP4文件
                            File fileMp4 = null;
                            try {
                                fileMp4 = File.createTempFile("mp4",".mp4");
                            } catch (IOException e) {
                                e.printStackTrace();
                                log.error("创建临时MP4文件失败");
                                mediaFileProcessService.saveProcessFinishStatus(taskId,"3",fileMd5,null,"创建临时MP4文件失败");
                                return;
                            }
                            //源文件转换MP4文件
                            String result = " ";
                            try {
                                //创建工具类对象
                                Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegpath, originalFile.getAbsolutePath(), fileMp4.getName(), fileMp4.getAbsolutePath());
                                //开始视频转换，成功将返回success
                                result = videoUtil.generateMp4();
                            } catch (Exception e) {
                                e.printStackTrace();
                                log.error("处理视频文件:{},出错:{}",filePath,e.getMessage());
                            }
                            //视频处理结果
                            if(!result.equals("success")){
                                log.error("处理视频文件失败,视频地址:{},错误信息:{}",bucket.concat(filePath),result);
                                mediaFileProcessService.saveProcessFinishStatus(taskId,"3",fileMd5,null,result);
                                return;
                            }

                            //将mp4上传至minio
                            //mp4在minio的存储路径
                            String objectName = this.getFilePath(fileMd5, ".mp4");
                            //访问url
                            String url = "/" + bucket + "/" + objectName;

                            try {
                                mediaFileService.addMediaFilesToMinIO(fileMp4.getAbsolutePath(), mediaFileService.getMimeType(".mp4"), bucket, objectName);
                                mediaFileProcessService.saveProcessFinishStatus(taskId, "2", fileMd5, url, null);
                            } catch (Exception e) {
                                log.error("上传视频失败或入库失败,视频地址:{},错误信息:{}", bucket + objectName, e.getMessage());
                                //最终还是失败了
                                mediaFileProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", fileMd5, null, "处理后视频上传或入库失败");
                            }
                        } finally {
                            countDownLatch.countDown();
                        }

                    }
            );
        }

        //阻塞,指定最大限制的等待时间，阻塞最多等待一定的时间后就解除阻塞
        countDownLatch.await(30, TimeUnit.MINUTES);



    }

    private String getFilePath(String fileMd5,String fileExt){
        return   fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +fileExt;
    }


}
