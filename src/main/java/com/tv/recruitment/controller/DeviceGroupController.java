package com.tv.recruitment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tv.recruitment.common.result.Result;
import com.tv.recruitment.entity.DeviceGroup;
import com.tv.recruitment.mapper.DeviceGroupMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 设备分组控制器
 */
@Tag(name = "设备分组管理")
@RestController
@RequestMapping("/devices/groups")
@RequiredArgsConstructor
public class DeviceGroupController {

    private final DeviceGroupMapper deviceGroupMapper;

    @Operation(summary = "获取分组列表")
    @GetMapping
    public Result<List<DeviceGroup>> list() {
        List<DeviceGroup> groups = deviceGroupMapper.selectList(
                new LambdaQueryWrapper<DeviceGroup>()
                        .orderByDesc(DeviceGroup::getCreateTime)
        );
        return Result.success(groups);
    }

    @Operation(summary = "获取分组详情")
    @GetMapping("/{id}")
    public Result<DeviceGroup> getById(@PathVariable Long id) {
        return Result.success(deviceGroupMapper.selectById(id));
    }

    @Operation(summary = "新增分组")
    @PostMapping
    public Result<Void> save(@RequestBody DeviceGroup group) {
        deviceGroupMapper.insert(group);
        return Result.success();
    }

    @Operation(summary = "编辑分组")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody DeviceGroup group) {
        group.setId(id);
        deviceGroupMapper.updateById(group);
        return Result.success();
    }

    @Operation(summary = "删除分组")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deviceGroupMapper.deleteById(id);
        return Result.success();
    }
}