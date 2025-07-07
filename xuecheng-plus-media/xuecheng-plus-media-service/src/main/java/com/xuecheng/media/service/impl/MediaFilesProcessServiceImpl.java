package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaFilesProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author hyb
 * @version 1.0
 * @description 媒资文件处理业务方法实现类
 * @date 2024/12/10
 */
@Slf4j
@Service
public class MediaFilesProcessServiceImpl implements MediaFilesProcessService {

    @Autowired
    MediaProcessMapper mediaProcessMapper;

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MediaProcessHistoryMapper mediaProcessHistoryMapper;

    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        return mediaProcessMapper.selectListByShardIndex(shardTotal, shardIndex, count);
    }

    @Override
    public boolean startTask(long id) {
        int i = mediaProcessMapper.startTask(id);
        return i <= 0 ? false : true;
    }

    @Transactional
    @Override
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        //查出任务，如果不存在则直接返回
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if(mediaProcess == null){
            return;
        }

        //处理失败，更新任务处理结果
        if(status.equals("3")){
            mediaProcess.setStatus(status);
            mediaProcess.setFailCount(mediaProcess.getFailCount() + 1);
            mediaProcess.setErrormsg(errorMsg);
            LambdaQueryWrapper<MediaProcess> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(MediaProcess::getId,taskId);
            mediaProcessMapper.update(mediaProcess, queryWrapper);
            log.debug("更新任务处理状态为失败，任务信息:{}",mediaProcess);

            return;
        }

        //任务处理成功
        //更新媒资文件中的访问url
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        if(mediaFiles != null){
            mediaFiles.setUrl(url);
            mediaFilesMapper.updateById(mediaFiles);
        }

        //处理成功，添加到mediaProcessHistory表，更新mediaProcessHistory的url和状态
        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
        mediaProcessHistory.setUrl(url);
        mediaProcessHistory.setStatus("2");
        mediaProcessHistory.setCreateDate(LocalDateTime.now());
        mediaProcessHistoryMapper.insert(mediaProcessHistory);

        //删除mediaProcess
        mediaProcessMapper.deleteById(taskId);
    }
}
