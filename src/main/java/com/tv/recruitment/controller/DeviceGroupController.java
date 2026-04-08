package com.tv.recruitment.controller;

import com.tv.recruitment.common.result.Result;
import com.tv.recruitment.entity.DeviceGroup;
import com.tv.recruitment.service.DeviceGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 设备分组控制器
 *
 * @author tv_recru
 */
@Tag(name = "设备分组管理")
@RestController
@RequestMapping("/devices/groups")
@RequiredArgsConstructor
@Validated
public class DeviceGroupController {

    private final DeviceGroupService deviceGroupService;

    @Operation(summary = "获取分组列表")
    @GetMapping
    public Result<List<DeviceGroup>> list() {
        return Result.success(deviceGroupService.listWithDeviceCount());
    }

    @Operation(summary = "获取分组详情")
    @GetMapping("/{id}")
    public Result<DeviceGroup> getById(@PathVariable Long id) {
        return Result.success(deviceGroupService.getDetailById(id));
    }

    @Operation(summary = "新增分组")
    @PostMapping
    public Result<Void> save(@RequestBody @Validated DeviceGroup group) {
        deviceGroupService.createGroup(group);
        return Result.success();
    }

    @Operation(summary = "编辑分组")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Validated DeviceGroup group) {
        deviceGroupService.updateGroup(id, group);
        return Result.success();
    }

    @Operation(summary = "删除分组")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deviceGroupService.deleteGroup(id);
        return Result.success();
    }
}
