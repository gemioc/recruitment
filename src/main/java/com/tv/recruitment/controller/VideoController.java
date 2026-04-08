package com.tv.recruitment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tv.recruitment.common.result.Result;
import com.tv.recruitment.entity.Video;
import com.tv.recruitment.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 视频控制器
 *
 * @author tv_recru
 */
@Tag(name = "视频管理")
@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @Operation(summary = "分页查询视频")
    @GetMapping
    public Result<Page<Video>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String videoName) {
        return Result.success(videoService.page(pageNum, pageSize, videoName));
    }

    @Operation(summary = "上传视频文件")
    @PostMapping("/upload")
    public Result<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        return Result.success(videoService.uploadVideo(file));
    }

    @Operation(summary = "创建视频记录")
    @PostMapping
    public Result<Video> create(@RequestBody Video video) {
        return Result.success(videoService.createVideo(video));
    }

    @Operation(summary = "编辑视频")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Video video) {
        videoService.updateVideo(id, video);
        return Result.success();
    }

    @Operation(summary = "删除视频")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        videoService.deleteVideo(id);
        return Result.success();
    }

    @Operation(summary = "置顶/取消置顶")
    @PutMapping("/{id}/top")
    public Result<Void> setTop(@PathVariable Long id, @RequestParam Integer isTop) {
        videoService.setTop(id, isTop);
        return Result.success();
    }

    @Operation(summary = "获取视频播放地址")
    @GetMapping("/{id}/play")
    public Result<String> getPlayUrl(@PathVariable Long id) {
        String playUrl = videoService.getPlayUrl(id);
        if (playUrl != null) {
            return Result.success(playUrl);
        }
        return Result.error("视频不存在");
    }
}
