package com.tv.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tv.recruitment.entity.Device;
import com.tv.recruitment.mapper.DeviceMapper;
import com.tv.recruitment.service.DeviceService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备服务实现
 */
@Service
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements DeviceService {

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

        return page(new Page<>(pageNum, pageSize), wrapper);
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
        LambdaUpdateWrapper<Device> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Device::getDeviceCode, deviceCode)
                .set(Device::getOnlineStatus, online ? 1 : 0)
                .set(Device::getLastHeartbeat, LocalDateTime.now());
        update(wrapper);
    }

    @Override
    public void restartDevice(Long id) {
        // TODO: 通过WebSocket发送重启指令到电视终端
        // Device device = getById(id);
        // webSocketHandler.sendControl(device.getDeviceCode(), "restart");
    }
}