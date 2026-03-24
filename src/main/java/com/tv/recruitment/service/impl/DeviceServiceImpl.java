package com.tv.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tv.recruitment.entity.Device;
import com.tv.recruitment.entity.DeviceGroup;
import com.tv.recruitment.mapper.DeviceGroupMapper;
import com.tv.recruitment.mapper.DeviceMapper;
import com.tv.recruitment.service.DeviceService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 设备服务实现
 */
@Service
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements DeviceService {

    private final DeviceGroupMapper deviceGroupMapper;

    public DeviceServiceImpl(DeviceGroupMapper deviceGroupMapper) {
        this.deviceGroupMapper = deviceGroupMapper;
    }

    @Override
    public Page<Device> page(Integer pageNum, Integer pageSize, String deviceCode, String deviceName,
                             String location, Long groupId, Integer onlineStatus, Integer status) {
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(deviceCode)) {
            wrapper.eq(Device::getDeviceCode, deviceCode);
        }
        if (StringUtils.hasText(deviceName)) {
            wrapper.like(Device::getDeviceName, deviceName);
        }
        if (StringUtils.hasText(location)) {
            wrapper.like(Device::getLocation, location);
        }
        if (groupId != null) {
            wrapper.eq(Device::getGroupId, groupId);
        }
        if (onlineStatus != null) {
            wrapper.eq(Device::getOnlineStatus, onlineStatus);
        }
        if (status != null) {
            wrapper.eq(Device::getStatus, status);
        }

        wrapper.orderByDesc(Device::getCreateTime);

        Page<Device> result = page(new Page<>(pageNum, pageSize), wrapper);

        // 设置分组名称
        List<Device> devices = result.getRecords();
        Set<Long> groupIds = devices.stream()
                .map(Device::getGroupId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        if (!groupIds.isEmpty()) {
            List<DeviceGroup> groups = deviceGroupMapper.selectBatchIds(groupIds);
            Map<Long, String> groupNameMap = groups.stream()
                    .collect(Collectors.toMap(DeviceGroup::getId, DeviceGroup::getGroupName));

            devices.forEach(device -> {
                if (device.getGroupId() != null) {
                    device.setGroupName(groupNameMap.get(device.getGroupId()));
                }
            });
        }

        return result;
    }

    @Override
    public Map<String, Object> getMonitor() {
        List<Device> devices = list();

        long total = devices.size();
        long onlineCount = devices.stream().filter(d -> d.getOnlineStatus() == 1).count();
        long offlineCount = total - onlineCount;
        long playingCount = devices.stream()
                .filter(d -> d.getOnlineStatus() == 1 && d.getPlayStatus() == 1)
                .count();
        long pauseCount = devices.stream()
                .filter(d -> d.getOnlineStatus() == 1 && d.getPlayStatus() == 2)
                .count();

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("onlineCount", onlineCount);
        result.put("offlineCount", offlineCount);
        result.put("playingCount", playingCount);
        result.put("pauseCount", pauseCount);

        return result;
    }

    @Override
    public void updateOnlineStatus(String deviceCode, boolean online) {
        Device device = getOne(new LambdaQueryWrapper<Device>().eq(Device::getDeviceCode, deviceCode));
        if (device == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<Device> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Device::getDeviceCode, deviceCode);

        if (online) {
            // 设备上线
            wrapper.set(Device::getOnlineStatus, 1)
                    .set(Device::getLastHeartbeat, now)
                    .set(Device::getLastOnlineTime, now);

            // 如果之前是离线状态，增加离线次数（因为这是一次新的上线，说明之前离线过）
            if (device.getOnlineStatus() != null && device.getOnlineStatus() == 0) {
                // 设备从离线变为在线，说明是一次新的连接
                wrapper.set(Device::getOfflineCount,
                        device.getOfflineCount() != null ? device.getOfflineCount() + 1 : 1);
            }
        } else {
            // 设备离线
            wrapper.set(Device::getOnlineStatus, 0)
                    .set(Device::getLastHeartbeat, now);

            // 计算本次在线时长并累加
            if (device.getLastOnlineTime() != null && device.getOnlineStatus() != null && device.getOnlineStatus() == 1) {
                // 之前是在线状态，计算在线时长
                long duration = java.time.Duration.between(device.getLastOnlineTime(), now).getSeconds();
                if (duration > 0) {
                    long totalDuration = device.getTotalOnlineDuration() != null ? device.getTotalOnlineDuration() : 0;
                    wrapper.set(Device::getTotalOnlineDuration, totalDuration + duration);
                }
            }

            // 清除当前播放内容信息
            wrapper.set(Device::getCurrentContentType, null)
                    .set(Device::getCurrentContentId, null)
                    .set(Device::getPlayStatus, null)
                    .set(Device::getContentStartTime, null);
        }

        update(wrapper);
    }

    /**
     * 更新设备心跳
     */
    public void updateHeartbeat(String deviceCode) {
        Device device = getOne(new LambdaQueryWrapper<Device>().eq(Device::getDeviceCode, deviceCode));
        if (device == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<Device> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Device::getDeviceCode, deviceCode)
                .set(Device::getLastHeartbeat, now);

        // 如果设备之前是离线状态，现在收到心跳说明设备上线了
        if (device.getOnlineStatus() == null || device.getOnlineStatus() == 0) {
            wrapper.set(Device::getOnlineStatus, 1)
                    .set(Device::getLastOnlineTime, now);
        }

        update(wrapper);
    }

    /**
     * 更新设备当前播放内容
     */
    public void updateCurrentContent(String deviceCode, Integer contentType, Long contentId) {
        LambdaUpdateWrapper<Device> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Device::getDeviceCode, deviceCode)
                .set(Device::getCurrentContentType, contentType)
                .set(Device::getCurrentContentId, contentId)
                .set(Device::getPlayStatus, 1)
                .set(Device::getContentStartTime, LocalDateTime.now());
        update(wrapper);
    }

    /**
     * 计算设备当前在线时长（秒）
     */
    public Long calculateCurrentOnlineDuration(Long deviceId) {
        Device device = getById(deviceId);
        if (device == null || device.getOnlineStatus() == null || device.getOnlineStatus() != 1) {
            return 0L;
        }

        if (device.getLastOnlineTime() == null) {
            return 0L;
        }

        // 累计在线时长 + 本次在线时长
        long totalDuration = device.getTotalOnlineDuration() != null ? device.getTotalOnlineDuration() : 0;
        long currentDuration = java.time.Duration.between(device.getLastOnlineTime(), LocalDateTime.now()).getSeconds();

        return totalDuration + currentDuration;
    }

    @Override
    public void restartDevice(Long id) {
        // TODO: 通过WebSocket发送重启指令到电视终端
        // Device device = getById(id);
        // webSocketHandler.sendControl(device.getDeviceCode(), "restart");
    }
}