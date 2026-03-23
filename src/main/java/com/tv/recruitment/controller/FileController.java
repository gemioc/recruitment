package com.tv.recruitment.controller;

import com.tv.recruitment.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 文件控制器
 */
@Tag(name = "文件管理")
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @Operation(summary = "访问图片")
    @GetMapping("/images/**")
    public ResponseEntity<Resource> getImage(HttpServletRequest request) {
        String filePath = extractFilePath(request, "/files/images/");
        Resource resource = fileStorageService.loadFile("images/" + filePath);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }

    @Operation(summary = "访问视频")
    @GetMapping("/videos/**")
    public ResponseEntity<Resource> getVideo(HttpServletRequest request) {
        String filePath = extractFilePath(request, "/files/videos/");
        Resource resource = fileStorageService.loadFile("videos/" + filePath);
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("video/mp4"))
                .body(resource);
    }

    @Operation(summary = "访问海报")
    @GetMapping("/posters/**")
    public ResponseEntity<Resource> getPoster(HttpServletRequest request) {
        String filePath = extractFilePath(request, "/files/posters/");
        Resource resource = fileStorageService.loadFile("posters/" + filePath);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(resource);
    }

    @Operation(summary = "下载文件")
    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestParam String path) {
        Resource resource = fileStorageService.loadFile(path);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    /**
     * 提取文件路径
     */
    private String extractFilePath(HttpServletRequest request, String prefix) {
        String uri = request.getRequestURI();
        return uri.substring(uri.indexOf(prefix) + prefix.length());
    }
}