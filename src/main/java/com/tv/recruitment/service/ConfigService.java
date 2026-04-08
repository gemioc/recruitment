package com.tv.recruitment.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 系统配置服务
 *
 * @author tv_recru
 */
public interface ConfigService {

    /**
     * 获取所有系统配置
     *
     * @return 配置Map，key为配置键，value为配置值
     */
    Map<String, String> getConfig();

    /**
     * 更新系统配置
     *
     * @param configs 配置Map
     */
    void updateConfig(Map<String, String> configs);

    /**
     * 上传公司Logo
     *
     * @param file Logo文件
     * @return Logo的URL
     */
    String uploadLogo(MultipartFile file);
}
