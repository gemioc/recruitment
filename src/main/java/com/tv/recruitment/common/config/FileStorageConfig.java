package com.tv.recruitment.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 文件存储配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "file.storage")
public class FileStorageConfig {

    /**
     * 文件存储根路径
     */
    private String basePath = "D:/tv-files";

    /**
     * 图片存储路径
     */
    private String imagePath = "/images";

    /**
     * 视频存储路径
     */
    private String videoPath = "/videos";

    /**
     * 海报存储路径
     */
    private String posterPath = "/posters";

    /**
     * 模板存储路径
     */
    private String templatePath = "/templates";

    /**
     * 文件最大大小（字节）
     */
    private Long maxSize = 200 * 1024 * 1024L;
}