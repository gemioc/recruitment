package com.tv.recruitment.service.impl;

import com.tv.recruitment.common.config.FileStorageConfig;
import com.tv.recruitment.common.exception.BusinessException;
import com.tv.recruitment.common.exception.ErrorCode;
import com.tv.recruitment.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件存储服务实现
 */
@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private final FileStorageConfig config;

    @Override
    public String saveFile(MultipartFile file, String type) {
        try {
            // 获取原始文件名
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                originalFilename = "unknown";
            }

            // 获取文件扩展名
            String extension = "";
            int dotIndex = originalFilename.lastIndexOf(".");
            if (dotIndex > 0) {
                extension = originalFilename.substring(dotIndex);
            }

            // 生成新文件名
            String fileName = UUID.randomUUID().toString().replace("-", "") + extension;

            // 按日期分目录
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
            String typePath = getTypePath(type);
            String relativePath = typePath + "/" + datePath;

            // 创建目录
            Path dirPath = Paths.get(config.getBasePath(), relativePath);
            Files.createDirectories(dirPath);

            // 保存文件
            Path filePath = dirPath.resolve(fileName);
            file.transferTo(filePath.toFile());

            return relativePath + "/" + fileName;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "文件保存失败: " + e.getMessage());
        }
    }

    @Override
    public Resource loadFile(String filePath) {
        try {
            Path path = Paths.get(config.getBasePath(), filePath);
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new BusinessException(ErrorCode.NOT_FOUND, "文件不存在");
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "文件路径错误");
        }
    }

    @Override
    public void deleteFile(String filePath) {
        try {
            Path path = Paths.get(config.getBasePath(), filePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // 忽略删除失败
        }
    }

    @Override
    public String getFileUrl(String filePath) {
        return "/files/" + filePath;
    }

    private String getTypePath(String type) {
        return switch (type) {
            case "images" -> config.getImagePath();
            case "videos" -> config.getVideoPath();
            case "posters" -> config.getPosterPath();
            case "templates" -> config.getTemplatePath();
            default -> "/" + type;
        };
    }
}