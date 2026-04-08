package com.tv.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tv.recruitment.entity.Device;
import com.tv.recruitment.entity.DeviceGroup;
import com.tv.recruitment.mapper.DeviceGroupMapper;
import com.tv.recruitment.mapper.DeviceMapper;
import com.tv.recruitment.service.DeviceGroupService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 设备分组服务实现
 *
 * @author tv_recru
 */
@Service
public class DeviceGroupServiceImpl extends ServiceImpl<DeviceGroupMapper, DeviceGroup> implements DeviceGroupService {

    private final DeviceGroupMapper deviceGroupMapper;
    private final DeviceMapper deviceMapper;

    public DeviceGroupServiceImpl(DeviceGroupMapper deviceGroupMapper, DeviceMapper deviceMapper) {
        this.deviceGroupMapper = deviceGroupMapper;
        this.deviceMapper = deviceMapper;
    }

    @Override
    public List<DeviceGroup> listWithDeviceCount() {
        // 查询所有分组
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

        return groups;
    }

    @Override
    public DeviceGroup getDetailById(Long id) {
        return deviceGroupMapper.selectById(id);
    }

    @Override
    public void createGroup(DeviceGroup group) {
        deviceGroupMapper.insert(group);
    }

    @Override
    public void updateGroup(Long id, DeviceGroup group) {
        group.setId(id);
        deviceGroupMapper.updateById(group);
    }

    @Override
    public void deleteGroup(Long id) {
        deviceGroupMapper.deleteById(id);
    }
}
