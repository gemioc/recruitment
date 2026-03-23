package com.tv.recruitment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tv.recruitment.common.result.Result;
import com.tv.recruitment.entity.SystemConfig;
import com.tv.recruitment.mapper.SystemConfigMapper;
import com.tv.recruitment.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置控制器
 */
@Tag(name = "系统配置")
@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class ConfigController {

    private final SystemConfigMapper configMapper;
    private final FileStorageService fileStorageService;

    @Operation(summary = "获取系统配置")
    @GetMapping
    public Result<Map<String, String>> getConfig() {
        List<SystemConfig> configs = configMapper.selectList(null);
        Map<String, String> result = new HashMap<>();
        configs.forEach(c -> result.put(c.getConfigKey(), c.getConfigValue()));
        return Result.success(result);
    }

    @Operation(summary = "更新系统配置")
    @PutMapping
    public Result<Void> updateConfig(@RequestBody Map<String, String> configs) {
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
        return Result.success();
    }

    @Operation(summary = "上传公司Logo")
    @PostMapping("/logo")
    public Result<String> uploadLogo(@RequestParam("file") MultipartFile file) {
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

        return Result.success(url);
    }
}