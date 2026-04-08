package com.tv.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tv.recruitment.entity.SystemConfig;
import com.tv.recruitment.mapper.SystemConfigMapper;
import com.tv.recruitment.service.ConfigService;
import com.tv.recruitment.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置服务实现
 *
 * @author tv_recru
 */
@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {

    private final SystemConfigMapper configMapper;
    private final FileStorageService fileStorageService;

    /**
     * 获取所有系统配置
     *
     * @return 配置Map，key为配置键，value为配置值
     */
    @Override
    public Map<String, String> getConfig() {
        List<SystemConfig> configs = configMapper.selectList(null);
        Map<String, String> result = new HashMap<>();
        configs.forEach(c -> result.put(c.getConfigKey(), c.getConfigValue()));
        return result;
    }

    /**
     * 更新系统配置
     *
     * @param configs 配置Map
     */
    @Override
    public void updateConfig(Map<String, String> configs) {
        configs.forEach((key, value) -> {
            SystemConfig config = configMapper.selectOne(
                    new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, key)
            );
            if (config != null) {
                config.setConfigValue(value);
                configMapper.updateById(config);
            } else {
                config = new SystemConfig();
                config.setConfigKey(key);
                config.setConfigValue(value);
                configMapper.insert(config);
            }
        });
    }

    /**
     * 上传公司Logo
     *
     * @param file Logo文件
     * @return Logo的URL
     */
    @Override
    public String uploadLogo(MultipartFile file) {
        String filePath = fileStorageService.saveFile(file, "images");
        String url = fileStorageService.getFileUrl(filePath);

        // 更新配置
        SystemConfig config = configMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, "company_logo")
        );
        if (config != null) {
            config.setConfigValue(url);
            configMapper.updateById(config);
        }

        return url;
    }
}
