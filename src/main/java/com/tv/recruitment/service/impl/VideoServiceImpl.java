package com.tv.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tv.recruitment.common.result.Result;
import com.tv.recruitment.entity.Video;
import com.tv.recruitment.mapper.VideoMapper;
import com.tv.recruitment.service.FileStorageService;
import com.tv.recruitment.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 视频服务实现
 *
 * @author tv_recru
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoService {

    private final VideoMapper videoMapper;
    private final FileStorageService fileStorageService;

    @Override
    public Page<Video> page(Integer pageNum, Integer pageSize, String videoName) {
        Page<Video> page = new Page<>(pageNum, pageSize);
        return videoMapper.selectPage(page,
                new LambdaQueryWrapper<Video>()
                        .like(videoName != null, Video::getVideoName, videoName)
                        .orderByDesc(Video::getIsTop)
                        .orderByDesc(Video::getCreateTime));
    }

    @Override
    public Map<String, Object> uploadVideo(MultipartFile file) {
        // 保存文件
        String filePath = fileStorageService.saveFile(file, "videos");

        Map<String, Object> result = new HashMap<>();
        result.put("filePath", filePath);
        result.put("fileSize", file.getSize());
        result.put("videoName", file.getOriginalFilename());

        return result;
    }

    @Override
    public Video createVideo(Video video) {
        videoMapper.insert(video);
        return video;
    }

    @Override
    public void updateVideo(Long id, Video video) {
        video.setId(id);
        videoMapper.updateById(video);
    }

    @Override
    public void deleteVideo(Long id) {
        Video video = videoMapper.selectById(id);
        if (video != null) {
            fileStorageService.deleteFile(video.getFilePath());
            videoMapper.deleteById(id);
        }
    }

    @Override
    public void setTop(Long id, Integer isTop) {
        Video video = new Video();
        video.setId(id);
        video.setIsTop(isTop);
        videoMapper.updateById(video);
    }

    @Override
    public String getPlayUrl(Long id) {
        Video video = videoMapper.selectById(id);
        if (video != null) {
            return fileStorageService.getFileUrl(video.getFilePath());
        }
        return null;
    }
}
