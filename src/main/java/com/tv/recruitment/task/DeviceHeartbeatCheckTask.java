package com.tv.recruitment.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tv.recruitment.entity.Device;
import com.tv.recruitment.mapper.DeviceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备心跳超时检测任务
 * 定时检测心跳超时的设备并将其标记为离线
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceHeartbeatCheckTask {

    private final DeviceMapper deviceMapper;

    /**
     * 心跳超时阈值（秒）
     * Android端心跳间隔30秒，设置3倍作为超时阈值
     */
    private static final long HEARTBEAT_TIMEOUT_SECONDS = 90;

    /**
     * 每60秒执行一次心跳超时检测
     */
    @Scheduled(fixedRate = 60000)
    public void checkHeartbeatTimeout() {
        log.debug("开始执行心跳超时检测...");

        LocalDateTime timeoutThreshold = LocalDateTime.now().minusSeconds(HEARTBEAT_TIMEOUT_SECONDS);
        LocalDateTime now = LocalDateTime.now();

        // 查找在线但心跳超时的设备
        LambdaQueryWrapper<Device> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Device::getOnlineStatus, 1)
                .isNotNull(Device::getLastHeartbeat)
                .lt(Device::getLastHeartbeat, timeoutThreshold);

        List<Device> timeoutDevices = deviceMapper.selectList(queryWrapper);

        if (timeoutDevices.isEmpty()) {
            return;
        }

        // 逐个更新设备状态，计算在线时长
        for (Device device : timeoutDevices) {
            LambdaUpdateWrapper<Device> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Device::getId, device.getId())
                    .set(Device::getOnlineStatus, 0)
                    .set(Device::getLastHeartbeat, now);

            // 计算本次在线时长并累加
            if (device.getLastOnlineTime() != null) {
                long duration = Duration.between(device.getLastOnlineTime(), now).getSeconds();
                if (duration > 0) {
                    long totalDuration = device.getTotalOnlineDuration() != null ? device.getTotalOnlineDuration() : 0;
                    updateWrapper.set(Device::getTotalOnlineDuration, totalDuration + duration);
                }
            }

            // 清除当前播放内容信息
            updateWrapper.set(Device::getCurrentContentType, null)
                    .set(Device::getCurrentContentId, null)
                    .set(Device::getPlayStatus, null)
                    .set(Device::getContentStartTime, null);

            deviceMapper.update(null, updateWrapper);
            log.info("设备心跳超时，已标记为离线: {}", device.getDeviceCode());
        }

        log.info("心跳超时检测完成，已将 {} 台设备标记为离线", timeoutDevices.size());
    }
}