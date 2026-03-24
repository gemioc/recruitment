package com.tv.recruitment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tv.recruitment.common.result.Result;
import com.tv.recruitment.entity.Video;
import com.tv.recruitment.mapper.VideoMapper;
import com.tv.recruitment.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 视频控制器
 */
@Tag(name = "视频管理")
@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoMapper videoMapper;
    private final FileStorageService fileStorageService;

    @Operation(summary = "分页查询视频")
    @GetMapping
    public Result<Page<Video>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String videoName) {
        Page<Video> page = new Page<>(pageNum, pageSize);
        Page<Video> result = videoMapper.selectPage(page,
                new LambdaQueryWrapper<Video>()
                        .like(videoName != null, Video::getVideoName, videoName)
                        .orderByDesc(Video::getIsTop)
                        .orderByDesc(Video::getCreateTime));
        return Result.success(result);
    }

    @Operation(summary = "上传视频文件")
    @PostMapping("/upload")
    public Result<java.util.Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        // 保存文件
        String filePath = fileStorageService.saveFile(file, "videos");

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("filePath", filePath);
        result.put("fileSize", file.getSize());
        result.put("videoName", file.getOriginalFilename());

        return Result.success(result);
    }

    @Operation(summary = "创建视频记录")
    @PostMapping
    public Result<Video> create(@RequestBody Video video) {
        videoMapper.insert(video);
        return Result.success(video);
    }

    @Operation(summary = "编辑视频")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Video video) {
        video.setId(id);
        videoMapper.updateById(video);
        return Result.success();
    }

    @Operation(summary = "删除视频")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Video video = videoMapper.selectById(id);
        if (video != null) {
            fileStorageService.deleteFile(video.getFilePath());
            videoMapper.deleteById(id);
        }
        return Result.success();
    }

    @Operation(summary = "置顶/取消置顶")
    @PutMapping("/{id}/top")
    public Result<Void> setTop(@PathVariable Long id, @RequestParam Integer isTop) {
        Video video = new Video();
        video.setId(id);
        video.setIsTop(isTop);
        videoMapper.updateById(video);
        return Result.success();
    }

    @Operation(summary = "获取视频播放地址")
    @GetMapping("/{id}/play")
    public Result<String> getPlayUrl(@PathVariable Long id) {
        Video video = videoMapper.selectById(id);
        if (video != null) {
            return Result.success(fileStorageService.getFileUrl(video.getFilePath()));
        }
        return Result.error("视频不存在");
    }
}