package com.tv.recruitment.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务
 */
public interface FileStorageService {

    /**
     * 保存文件
     *
     * @param file 文件
     * @param type 类型: images/videos/posters/templates
     * @return 相对路径
     */
    String saveFile(MultipartFile file, String type);

    /**
     * 加载文件
     *
     * @param filePath 相对路径
     * @return Resource
     */
    Resource loadFile(String filePath);

    /**
     * 删除文件
     *
     * @param filePath 相对路径
     */
    void deleteFile(String filePath);

    /**
     * 获取文件的完整URL
     *
     * @param filePath 相对路径
     * @return 完整URL
     */
    String getFileUrl(String filePath);
}