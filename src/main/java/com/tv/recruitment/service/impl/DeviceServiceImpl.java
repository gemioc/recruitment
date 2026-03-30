package com.tv.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tv.recruitment.entity.Device;
import com.tv.recruitment.entity.DeviceGroup;
import com.tv.recruitment.entity.Poster;
import com.tv.recruitment.entity.Video;
import com.tv.recruitment.mapper.DeviceGroupMapper;
import com.tv.recruitment.mapper.DeviceMapper;
import com.tv.recruitment.mapper.PosterMapper;
import com.tv.recruitment.mapper.VideoMapper;
import com.tv.recruitment.service.DeviceService;
import com.tv.recruitment.websocket.WebSocketHandler;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 设备服务实现
 */
@Service
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements DeviceService {

    private final DeviceGroupMapper deviceGroupMapper;
    private final PosterMapper posterMapper;
    private final VideoMapper videoMapper;
    private final WebSocketHandler webSocketHandler;

    public DeviceServiceImpl(DeviceGroupMapper deviceGroupMapper, PosterMapper posterMapper,
                             VideoMapper videoMapper, @Lazy WebSocketHandler webSocketHandler) {
        this.deviceGroupMapper = deviceGroupMapper;
        this.posterMapper = posterMapper;
        this.videoMapper = videoMapper;
        this.webSocketHandler = webSocketHandler;
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

        // 设置分组名称和当前播放内容名称
        fillDeviceExtraInfo(result.getRecords());

        return result;
    }

    /**
     * 填充设备额外信息（分组名称、当前播放内容名称）
     */
    private void fillDeviceExtraInfo(List<Device> devices) {
        if (devices == null || devices.isEmpty()) {
            return;
        }

        // 设置分组名称
        Set<Long> groupIds = devices.stream()
                .map(Device::getGroupId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> groupNameMap = new HashMap<>();
        if (!groupIds.isEmpty()) {
            List<DeviceGroup> groups = deviceGroupMapper.selectBatchIds(groupIds);
            groupNameMap = groups.stream()
                    .collect(Collectors.toMap(DeviceGroup::getId, DeviceGroup::getGroupName));
        }

        // 收集所有需要查询的内容ID
        Set<Long> posterIds = new HashSet<>();
        Set<Long> videoIds = new HashSet<>();
        for (Device device : devices) {
            if (device.getCurrentContentId() != null && device.getCurrentContentType() != null) {
                if (device.getCurrentContentType() == 1) {
                    posterIds.add(device.getCurrentContentId());
                } else if (device.getCurrentContentType() == 2) {
                    videoIds.add(device.getCurrentContentId());
                }
            }
        }

        // 批量查询内容名称
        Map<Long, String> posterNameMap = new HashMap<>();
        Map<Long, String> videoNameMap = new HashMap<>();

        if (!posterIds.isEmpty()) {
            List<Poster> posters = posterMapper.selectBatchIds(posterIds);
            posterNameMap = posters.stream()
                    .collect(Collectors.toMap(Poster::getId, Poster::getPosterName));
        }

        if (!videoIds.isEmpty()) {
            List<Video> videos = videoMapper.selectBatchIds(videoIds);
            videoNameMap = videos.stream()
                    .collect(Collectors.toMap(Video::getId, Video::getVideoName));
        }

        // 设置设备的额外字段
        for (Device device : devices) {
            // 设置分组名称
            if (device.getGroupId() != null) {
                device.setGroupName(groupNameMap.get(device.getGroupId()));
            }

            // 设置当前播放内容名称
            if (device.getCurrentContentId() != null && device.getCurrentContentType() != null) {
                String contentName = null;
                if (device.getCurrentContentType() == 1) {
                    contentName = posterNameMap.get(device.getCurrentContentId());
                } else if (device.getCurrentContentType() == 2) {
                    contentName = videoNameMap.get(device.getCurrentContentId());
                }
                device.setCurrentContent(contentName);
            }
        }
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
        Device device = getById(id);
        if (device == null) {
            throw new RuntimeException("设备不存在");
        }
        // 通过WebSocket发送重启指令到电视终端
        webSocketHandler.sendControl(device.getDeviceCode(), "restart");
    }

    @Override
    public Device getDetailById(Long id) {
        Device device = getById(id);
        if (device == null) {
            return null;
        }
        // 填充额外信息
        fillDeviceExtraInfo(List.of(device));
        return device;
    }
}