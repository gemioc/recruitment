package com.tv.recruitment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tv.recruitment.common.result.Result;
import com.tv.recruitment.entity.Image;
import com.tv.recruitment.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 图片控制器
 *
 * @author tv_recru
 */
@Tag(name = "图片管理")
@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @Operation(summary = "分页查询图片")
    @GetMapping
    public Result<Page<Image>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String imageName) {
        return Result.success(imageService.page(pageNum, pageSize, imageName));
    }

    @Operation(summary = "上传图片文件")
    @PostMapping("/upload")
    public Result<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        return Result.success(imageService.uploadImage(file));
    }

    @Operation(summary = "创建图片记录")
    @PostMapping
    public Result<Image> create(@RequestBody Image image) {
        return Result.success(imageService.createImage(image));
    }

    @Operation(summary = "编辑图片")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Image image) {
        imageService.updateImage(id, image);
        return Result.success();
    }

    @Operation(summary = "删除图片")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        imageService.deleteImage(id);
        return Result.success();
    }

    @Operation(summary = "置顶/取消置顶")
    @PutMapping("/{id}/top")
    public Result<Void> setTop(@PathVariable Long id, @RequestParam Integer isTop) {
        imageService.setTop(id, isTop);
        return Result.success();
    }

    @Operation(summary = "获取图片访问地址")
    @GetMapping("/{id}/url")
    public Result<String> getImageUrl(@PathVariable Long id) {
        String imageUrl = imageService.getImageUrl(id);
        if (imageUrl != null) {
            return Result.success(imageUrl);
        }
        return Result.error("图片不存在");
    }
}