package com.tv.recruitment.common.config;

import jakarta.annotation.PostConstruct;
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
    private String basePath;

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

    @PostConstruct
    public void init() {
        if (basePath == null || basePath.isEmpty()) {
            // 优先级: 环境变量 > 系统属性 > 自动检测
            String envPath = System.getenv("TV_FILES_PATH");
            String propPath = System.getProperty("file.storage.base-path");

            if (envPath != null && !envPath.isEmpty()) {
                // 生产环境(Docker): 使用环境变量
                basePath = envPath;
            } else if (propPath != null && !propPath.isEmpty()) {
                // 启动参数: -Dfile.storage.base-path=/path
                basePath = propPath;
            } else {
                // 开发环境(dev): 自动检测系统类型
                String osName = System.getProperty("os.name", "").toLowerCase();
                if (osName.contains("win")) {
                    String userDir = System.getProperty("user.dir", "D:/");
                    basePath = userDir + "/tv-files";
                } else {
                    basePath = "/home/tv_recru/tv-files";
                }
            }
        }
    }
}