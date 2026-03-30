package com.tv.recruitment.controller;

import com.tv.recruitment.common.result.Result;
import com.tv.recruitment.service.PendingDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 待注册设备控制器
 */
@Tag(name = "待注册设备")
@RestController
@RequestMapping("/pending-devices")
@RequiredArgsConstructor
public class PendingDeviceController {

    private final PendingDeviceService pendingDeviceService;

    @Operation(summary = "获取待注册设备列表")
    @GetMapping
    public Result<List<Map<String, Object>>> getPendingDevices() {
        return Result.success(pendingDeviceService.getPendingDevices());
    }
}