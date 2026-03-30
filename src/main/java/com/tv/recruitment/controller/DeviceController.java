package com.tv.recruitment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tv.recruitment.common.result.Result;
import com.tv.recruitment.entity.Device;
import com.tv.recruitment.service.DeviceService;
import com.tv.recruitment.service.PendingDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 设备控制器
 */
@Tag(name = "设备管理")
@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;
    private final PendingDeviceService pendingDeviceService;

    @Operation(summary = "分页查询设备")
    @GetMapping
    public Result<Page<Device>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String deviceCode,
            @RequestParam(required = false) String deviceName,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) Integer onlineStatus,
            @RequestParam(required = false) Integer status) {
        return Result.success(deviceService.page(pageNum, pageSize, deviceCode, deviceName, location, groupId, onlineStatus, status));
    }

    @Operation(summary = "获取设备详情")
    @GetMapping("/{id}")
    public Result<Device> getById(@PathVariable Long id) {
        return Result.success(deviceService.getDetailById(id));
    }

    @Operation(summary = "新增设备")
    @PostMapping
    public Result<Void> save(@RequestBody Device device) {
        deviceService.save(device);
        // 设备注册成功，从待注册列表移除
        if (device.getDeviceCode() != null) {
            pendingDeviceService.removePendingDevice(device.getDeviceCode());
        }
        return Result.success();
    }

    @Operation(summary = "编辑设备")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Device device) {
        device.setId(id);
        deviceService.updateById(device);
        return Result.success();
    }

    @Operation(summary = "删除设备")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deviceService.removeById(id);
        return Result.success();
    }

    @Operation(summary = "重启设备")
    @PostMapping("/{id}/restart")
    public Result<Void> restart(@PathVariable Long id) {
        deviceService.restartDevice(id);
        return Result.success();
    }

    @Operation(summary = "更改使用状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Device device = new Device();
        device.setId(id);
        device.setStatus(status);
        deviceService.updateById(device);
        return Result.success();
    }

    @Operation(summary = "实时监控统计")
    @GetMapping("/monitor")
    public Result<Map<String, Object>> getMonitor() {
        return Result.success(deviceService.getMonitor());
    }
}