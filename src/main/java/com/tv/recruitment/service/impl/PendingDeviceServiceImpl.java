package com.tv.recruitment.service.impl;

import com.tv.recruitment.entity.Device;
import com.tv.recruitment.mapper.DeviceMapper;
import com.tv.recruitment.service.PendingDeviceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 待注册设备服务实现
 * 使用内存存储待注册设备列表
 */
@Service
@RequiredArgsConstructor
public class PendingDeviceServiceImpl implements PendingDeviceService {

    private final DeviceMapper deviceMapper;

    /**
     * 待注册设备缓存
     * key: deviceCode, value: 设备信息
     */
    private static final Map<String, Map<String, Object>> PENDING_DEVICES = new ConcurrentHashMap<>();

    /**
     * 设备信息过期时间（毫秒）- 30分钟
     */
    private static final long EXPIRE_TIME = 30 * 60 * 1000L;

    @Override
    public void addPendingDevice(String deviceCode, Map<String, Object> deviceInfo) {
        Map<String, Object> info = new HashMap<>();
        info.put("deviceCode", deviceCode);
        info.put("connectTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        info.put("timestamp", System.currentTimeMillis());
        if (deviceInfo != null) {
            info.putAll(deviceInfo);
        }
        PENDING_DEVICES.put(deviceCode, info);
    }

    @Override
    public void removePendingDevice(String deviceCode) {
        PENDING_DEVICES.remove(deviceCode);
    }

    @Override
    public List<Map<String, Object>> getPendingDevices() {
        // 清理过期的待注册设备
        cleanExpiredDevices();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> device : PENDING_DEVICES.values()) {
            // 过滤掉已注册的设备
            String deviceCode = (String) device.get("deviceCode");
            if (deviceCode != null && !isRegistered(deviceCode)) {
                result.add(device);
            }
        }
        // 按连接时间倒序排列
        result.sort((a, b) -> {
            String timeA = (String) a.get("connectTime");
            String timeB = (String) b.get("connectTime");
            if (timeA == null) return 1;
            if (timeB == null) return -1;
            return timeB.compareTo(timeA);
        });
        return result;
    }

    @Override
    public boolean isRegistered(String deviceCode) {
        if (deviceCode == null) {
            return false;
        }
        Long count = deviceMapper.selectCount(
            new LambdaQueryWrapper<Device>().eq(Device::getDeviceCode, deviceCode)
        );
        return count != null && count > 0;
    }

    /**
     * 清理过期的待注册设备
     */
    private void cleanExpiredDevices() {
        long now = System.currentTimeMillis();
        PENDING_DEVICES.entrySet().removeIf(entry -> {
            Long timestamp = (Long) entry.getValue().get("timestamp");
            return timestamp == null || (now - timestamp) > EXPIRE_TIME;
        });
    }
}