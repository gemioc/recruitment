package com.tv.recruitment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tv.recruitment.common.result.Result;
import com.tv.recruitment.entity.Device;
import com.tv.recruitment.entity.DeviceGroup;
import com.tv.recruitment.mapper.DeviceGroupMapper;
import com.tv.recruitment.mapper.DeviceMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 设备分组控制器
 */
@Tag(name = "设备分组管理")
@RestController
@RequestMapping("/devices/groups")
@RequiredArgsConstructor
public class DeviceGroupController {

    private final DeviceGroupMapper deviceGroupMapper;
    private final DeviceMapper deviceMapper;

    @Operation(summary = "获取分组列表")
    @GetMapping
    public Result<List<DeviceGroup>> list() {
        List<DeviceGroup> groups = deviceGroupMapper.selectList(
                new LambdaQueryWrapper<DeviceGroup>()
                        .orderByDesc(DeviceGroup::getCreateTime)
        );

        // 统计每个分组的设备数量和在线数量
        List<Device> allDevices = deviceMapper.selectList(null);
        Map<Long, List<Device>> devicesByGroup = allDevices.stream()
                .filter(d -> d.getGroupId() != null)
                .collect(Collectors.groupingBy(Device::getGroupId));

        for (DeviceGroup group : groups) {
            List<Device> groupDevices = devicesByGroup.getOrDefault(group.getId(), List.of());
            group.setDeviceCount(groupDevices.size());
            group.setOnlineCount((int) groupDevices.stream()
                    .filter(d -> d.getOnlineStatus() != null && d.getOnlineStatus() == 1)
                    .count());
        }

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