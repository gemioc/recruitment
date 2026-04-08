package com.tv.recruitment.controller;

import com.tv.recruitment.common.result.Result;
import com.tv.recruitment.service.ConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 系统配置控制器
 *
 * @author tv_recru
 */
@Tag(name = "系统配置")
@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService;

    /**
     * 获取系统配置
     *
     * @return 系统配置Map
     */
    @Operation(summary = "获取系统配置")
    @GetMapping
    public Result<Map<String, String>> getConfig() {
        return Result.success(configService.getConfig());
    }

    /**
     * 更新系统配置
     *
     * @param configs 配置Map
     * @return 操作结果
     */
    @Operation(summary = "更新系统配置")
    @PutMapping
    public Result<Void> updateConfig(@RequestBody Map<String, String> configs) {
        configService.updateConfig(configs);
        return Result.success();
    }

    /**
     * 上传公司Logo
     *
     * @param file Logo文件
     * @return Logo的URL
     */
    @Operation(summary = "上传公司Logo")
    @PostMapping("/logo")
    public Result<String> uploadLogo(@RequestParam("file") MultipartFile file) {
        return Result.success(configService.uploadLogo(file));
    }
}
