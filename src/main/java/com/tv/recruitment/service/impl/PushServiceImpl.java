package com.tv.recruitment.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tv.recruitment.common.enums.PushStatusEnum;
import com.tv.recruitment.common.utils.SecurityUtils;
import com.tv.recruitment.dto.request.ControlRequest;
import com.tv.recruitment.dto.request.PushRequest;
import com.tv.recruitment.dto.response.PushRecordResponse;
import com.tv.recruitment.entity.Device;
import com.tv.recruitment.entity.DeviceGroup;
import com.tv.recruitment.entity.Poster;
import com.tv.recruitment.entity.PushRecord;
import com.tv.recruitment.entity.User;
import com.tv.recruitment.entity.Video;
import com.tv.recruitment.mapper.DeviceGroupMapper;
import com.tv.recruitment.mapper.DeviceMapper;
import com.tv.recruitment.mapper.PosterMapper;
import com.tv.recruitment.mapper.PushRecordMapper;
import com.tv.recruitment.mapper.UserMapper;
import com.tv.recruitment.mapper.VideoMapper;
import com.tv.recruitment.service.PushService;
import com.tv.recruitment.websocket.WebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 推送服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PushServiceImpl implements PushService {

    private final PushRecordMapper pushRecordMapper;
    private final WebSocketHandler webSocketHandler;
    private final PosterMapper posterMapper;
    private final VideoMapper videoMapper;
    private final DeviceMapper deviceMapper;
    private final DeviceGroupMapper deviceGroupMapper;
    private final UserMapper userMapper;

    @Override
    public Long pushPoster(PushRequest request) {
        // 获取海报名称
        String contentName = null;
        if (request.getPosterId() != null) {
            Poster poster = posterMapper.selectById(request.getPosterId());
            if (poster != null) {
                contentName = poster.getPosterName();
            }
        }

        // 处理分组推送
        List<Long> targetIds = resolveTargetIds(request);

        // 创建推送记录
        PushRecord record = new PushRecord();
        record.setContentType(1);
        record.setContentId(request.getPosterId());
        record.setContentName(contentName);
        record.setPushType(determinePushType(request));
        record.setGroupId(request.getGroupId());
        record.setTargetIds(JSONUtil.toJsonStr(targetIds));
        record.setPlayRule(JSONUtil.toJsonStr(request.getPlayRule()));
        record.setDeviceCount(targetIds.size());
        record.setPushStatus(0);
        record.setPushTime(LocalDateTime.now());
        record.setPushBy(SecurityUtils.getCurrentUserId());
        pushRecordMapper.insert(record);

        // 执行推送
        executePush(record, targetIds);

        return record.getId();
    }

    @Override
    public Long pushVideo(PushRequest request) {
        // 获取视频名称
        String contentName = null;
        if (request.getVideoId() != null) {
            Video video = videoMapper.selectById(request.getVideoId());
            if (video != null) {
                contentName = video.getVideoName();
            }
        }

        // 处理分组推送
        List<Long> targetIds = resolveTargetIds(request);

        PushRecord record = new PushRecord();
        record.setContentType(2);
        record.setContentId(request.getVideoId());
        record.setContentName(contentName);
        record.setPushType(determinePushType(request));
        record.setGroupId(request.getGroupId());
        record.setTargetIds(JSONUtil.toJsonStr(targetIds));
        record.setPlayRule(JSONUtil.toJsonStr(request.getPlayRule()));
        record.setDeviceCount(targetIds.size());
        record.setPushStatus(0);
        record.setPushTime(LocalDateTime.now());
        record.setPushBy(SecurityUtils.getCurrentUserId());
        pushRecordMapper.insert(record);

        executePush(record, targetIds);

        return record.getId();
    }

    @Override
    public void control(ControlRequest request) {
        for (Long deviceId : request.getDeviceIds()) {
            Device device = deviceMapper.selectById(deviceId);
            if (device != null) {
                webSocketHandler.sendControl(device.getDeviceCode(), request.getAction());
            }
        }
    }

    @Override
    public Page<PushRecordResponse> getRecords(Integer pageNum, Integer pageSize, Integer contentType,
            Integer pushStatus, String startDate, String endDate, String deviceName) {
        Page<PushRecord> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<PushRecord> wrapper = buildQueryWrapper(contentType, pushStatus, startDate, endDate, deviceName);
        Page<PushRecord> result = pushRecordMapper.selectPage(page, wrapper);

        // 转换为响应对象
        Page<PushRecordResponse> responsePage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<PushRecordResponse> records = result.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        responsePage.setRecords(records);

        return responsePage;
    }

    @Override
    public PushRecordResponse getRecordDetail(Long id) {
        PushRecord record = pushRecordMapper.selectById(id);
        if (record == null) {
            return null;
        }
        return convertToResponse(record);
    }

    @Override
    public Long pushMultiple(PushRequest request) {
        // 转换contentType: poster -> 1, video -> 2
        int contentTypeInt = "video".equals(request.getContentType()) ? 2 : 1;

        // 获取所有内容ID
        List<Long> contentIds = request.getContentIds();
        if (contentIds == null || contentIds.isEmpty()) {
            // 兼容单个contentId的情况
            if (request.getContentId() != null) {
                contentIds = List.of(request.getContentId());
            } else if (contentTypeInt == 1 && request.getPosterId() != null) {
                contentIds = List.of(request.getPosterId());
            } else if (contentTypeInt == 2 && request.getVideoId() != null) {
                contentIds = List.of(request.getVideoId());
            }
        }

        if (contentIds == null || contentIds.isEmpty()) {
            throw new IllegalArgumentException("请选择要推送的内容");
        }

        // 获取内容名称列表
        List<String> contentNames = new ArrayList<>();
        for (Long contentId : contentIds) {
            if (contentTypeInt == 1) {
                Poster poster = posterMapper.selectById(contentId);
                if (poster != null) {
                    contentNames.add(poster.getPosterName());
                }
            } else {
                Video video = videoMapper.selectById(contentId);
                if (video != null) {
                    contentNames.add(video.getVideoName());
                }
            }
        }

        // 处理分组推送
        List<Long> targetIds = resolveTargetIds(request);

        if (targetIds.isEmpty()) {
            throw new IllegalArgumentException("请选择推送目标");
        }

        PushRecord record = new PushRecord();
        record.setContentType(contentTypeInt);
        // 存储第一个内容ID作为主键，所有内容ID存储在content_ids字段
        record.setContentId(contentIds.get(0));
        record.setContentName(String.join("、", contentNames));
        record.setPushType(determinePushType(request));
        record.setGroupId(request.getGroupId());
        record.setTargetIds(JSONUtil.toJsonStr(targetIds));
        record.setPlayRule(JSONUtil.toJsonStr(request.getPlayRule()));
        record.setDeviceCount(targetIds.size());
        record.setPushStatus(0);
        record.setPushTime(LocalDateTime.now());
        record.setPushBy(SecurityUtils.getCurrentUserId());
        pushRecordMapper.insert(record);

        // 执行推送（支持多内容）
        executePushMultiple(record, targetIds, contentIds, contentTypeInt);

        return record.getId();
    }

    @Override
    public List<Long> getDeviceIdsByGroup(Long groupId) {
        List<Device> devices = deviceMapper.selectList(
                new LambdaQueryWrapper<Device>()
                        .eq(Device::getGroupId, groupId)
                        .eq(Device::getStatus, 1)
        );
        List<Long> deviceIds = devices.stream()
                .map(Device::getId)
                .collect(Collectors.toList());
        return deviceIds;
    }

    @Override
    public List<Map<String, Object>> getPushGroups() {
        List<DeviceGroup> groups = deviceGroupMapper.selectList(
                new LambdaQueryWrapper<DeviceGroup>().orderByAsc(DeviceGroup::getCreateTime)
        );

        List<Map<String, Object>> result = new ArrayList<>();
        for (DeviceGroup group : groups) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", group.getId());
            item.put("groupName", group.getGroupName());
            item.put("description", group.getDescription());

            // 统计设备数量
            Long totalCount = deviceMapper.selectCount(
                    new LambdaQueryWrapper<Device>()
                            .eq(Device::getGroupId, group.getId())
                            .eq(Device::getStatus, 1)
            );
            Long onlineCount = deviceMapper.selectCount(
                    new LambdaQueryWrapper<Device>()
                            .eq(Device::getGroupId, group.getId())
                            .eq(Device::getStatus, 1)
                            .eq(Device::getOnlineStatus, 1)
            );

            item.put("deviceCount", totalCount);
            item.put("onlineCount", onlineCount);
            result.add(item);
        }

        return result;
    }

    @Override
    public List<Long> resolveTargetIds(PushRequest request) {
        // 如果指定了分组ID，查询分组下所有设备
        if (request.getGroupId() != null) {
            List<Device> devices = deviceMapper.selectList(
                    new LambdaQueryWrapper<Device>()
                            .eq(Device::getGroupId, request.getGroupId())
                            .eq(Device::getStatus, 1)
            );
            return devices.stream()
                    .map(Device::getId)
                    .collect(Collectors.toList());
        }

        // 否则使用传入的设备ID列表
        if (request.getTargetIds() != null) {
            return request.getTargetIds();
        }

        return new ArrayList<>();
    }

    @Override
    public LambdaQueryWrapper<PushRecord> buildQueryWrapper(Integer contentType, Integer pushStatus,
            String startDate, String endDate, String deviceName) {
        LambdaQueryWrapper<PushRecord> wrapper = new LambdaQueryWrapper<PushRecord>()
                .eq(contentType != null, PushRecord::getContentType, contentType)
                .eq(pushStatus != null, PushRecord::getPushStatus, pushStatus);

        // 日期筛选
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(PushRecord::getPushTime, startDate + " 00:00:00");
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(PushRecord::getPushTime, endDate + " 23:59:59");
        }

        // 设备名称筛选
        if (deviceName != null && !deviceName.isEmpty()) {
            List<Device> devices = deviceMapper.selectList(
                    new LambdaQueryWrapper<Device>()
                            .like(Device::getDeviceName, deviceName)
            );
            if (!devices.isEmpty()) {
                List<Long> deviceIds = devices.stream()
                        .map(Device::getId)
                        .collect(Collectors.toList());
                wrapper.and(w -> {
                    for (int i = 0; i < deviceIds.size(); i++) {
                        if (i == 0) {
                            w.like(PushRecord::getTargetIds, deviceIds.get(i).toString());
                        } else {
                            w.or().like(PushRecord::getTargetIds, deviceIds.get(i).toString());
                        }
                    }
                });
            } else {
                wrapper.apply("1 = 0");
            }
        }

        wrapper.orderByDesc(PushRecord::getPushTime);
        return wrapper;
    }

    @Override
    public void executePush(PushRecord record, List<Long> targetIds) {
        if (targetIds == null || targetIds.isEmpty()) {
            return;
        }

        // 获取内容URL
        String contentUrl = null;
        String contentType = record.getContentType() == 1 ? "poster" : "video";

        if (record.getContentId() != null) {
            if (record.getContentType() == 1) {
                Poster poster = posterMapper.selectById(record.getContentId());
                if (poster != null) {
                    contentUrl = poster.getFilePath();
                }
            } else {
                Video video = videoMapper.selectById(record.getContentId());
                if (video != null) {
                    contentUrl = video.getFilePath();
                }
            }
        }

        if (contentUrl == null) {
            log.warn("内容不存在或已删除，无法推送: contentId={}", record.getContentId());
            return;
        }

        // 转换为文件访问路径：添加 /files 前缀
        // filePath 格式: /posters/xxx.png 或 posters/xxx.png
        // 访问路径格式: /files/posters/xxx.png
        if (!contentUrl.startsWith("/files")) {
            contentUrl = "/files" + (contentUrl.startsWith("/") ? contentUrl : "/" + contentUrl);
        }

        // 推送到每个设备
        int successCount = 0;
        int failCount = 0;

        for (Long deviceId : targetIds) {
            Device device = deviceMapper.selectById(deviceId);
            if (device == null) {
                failCount++;
                continue;
            }

            if (webSocketHandler.isOnline(device.getDeviceCode())) {
                try {
                    webSocketHandler.pushContent(
                            device.getDeviceCode(),
                            contentType,
                            contentUrl,
                            record.getPlayRule()
                    );

                    // 更新设备当前播放内容
                    device.setCurrentContentType(record.getContentType());
                    device.setCurrentContentId(record.getContentId());
                    device.setPlayStatus(1);
                    device.setContentStartTime(LocalDateTime.now());
                    deviceMapper.updateById(device);

                    successCount++;
                } catch (Exception e) {
                    log.error("推送失败: deviceCode={}, error={}", device.getDeviceCode(), e.getMessage());
                    failCount++;
                }
            } else {
                log.warn("设备不在线: deviceCode={}", device.getDeviceCode());
                failCount++;
            }
        }

        // 更新推送记录状态
        record.setSuccessCount(successCount);
        record.setFailCount(failCount);
        record.setPushStatus(failCount == 0 ? 1 : (successCount == 0 ? 2 : 1));
        record.setCompleteTime(LocalDateTime.now());
        pushRecordMapper.updateById(record);
    }

    @Override
    public void executePushMultiple(PushRecord record, List<Long> targetIds, List<Long> contentIds, int contentTypeInt) {
        if (targetIds == null || targetIds.isEmpty() || contentIds == null || contentIds.isEmpty()) {
            return;
        }

        String contentType = contentTypeInt == 1 ? "poster" : "video";

        // 获取所有内容URL
        List<String> contentUrls = new ArrayList<>();
        for (Long contentId : contentIds) {
            String url = null;
            if (contentTypeInt == 1) {
                Poster poster = posterMapper.selectById(contentId);
                if (poster != null) {
                    url = poster.getFilePath();
                }
            } else {
                Video video = videoMapper.selectById(contentId);
                if (video != null) {
                    url = video.getFilePath();
                }
            }

            if (url != null) {
                // 转换为文件访问路径：添加 /files 前缀
                if (!url.startsWith("/files")) {
                    url = "/files" + (url.startsWith("/") ? url : "/" + url);
                }
                contentUrls.add(url);
            }
        }

        if (contentUrls.isEmpty()) {
            log.warn("没有有效的内容可推送");
            return;
        }

        // 推送到每个设备
        int successCount = 0;
        int failCount = 0;

        for (Long deviceId : targetIds) {
            Device device = deviceMapper.selectById(deviceId);
            if (device == null) {
                failCount++;
                continue;
            }

            if (webSocketHandler.isOnline(device.getDeviceCode())) {
                try {
                    // 使用新的多内容推送方法
                    webSocketHandler.pushMultipleContents(
                            device.getDeviceCode(),
                            contentType,
                            contentUrls,
                            record.getPlayRule()
                    );

                    // 更新设备当前播放内容
                    device.setCurrentContentType(contentTypeInt);
                    device.setCurrentContentId(contentIds.get(0));
                    device.setPlayStatus(1);
                    device.setContentStartTime(LocalDateTime.now());
                    deviceMapper.updateById(device);

                    successCount++;
                } catch (Exception e) {
                    log.error("推送失败: deviceCode={}, error={}", device.getDeviceCode(), e.getMessage());
                    failCount++;
                }
            } else {
                log.warn("设备不在线: deviceCode={}", device.getDeviceCode());
                failCount++;
            }
        }

        // 更新推送记录状态
        record.setSuccessCount(successCount);
        record.setFailCount(failCount);
        record.setPushStatus(failCount == 0 ? 1 : (successCount == 0 ? 2 : 1));
        record.setCompleteTime(LocalDateTime.now());
        pushRecordMapper.updateById(record);
    }

    @Override
    public Integer determinePushType(PushRequest request) {
        // 如果指定了分组ID，则为分组推送
        if (request.getGroupId() != null) {
            return 3;
        }
        // 根据目标设备数量判断
        List<Long> targetIds = request.getTargetIds();
        if (targetIds == null || targetIds.isEmpty()) {
            return 1; // 默认单台
        }
        return targetIds.size() == 1 ? 1 : 2;
    }

    @Override
    public String getPushTypeText(Integer pushType) {
        if (pushType == null) return "未知";
        switch (pushType) {
            case 1: return "单台推送";
            case 2: return "多台推送";
            case 3: return "分组推送";
            default: return "未知";
        }
    }

    @Override
    public String getStatusText(Integer status) {
        if (status == null) return "未知";
        PushStatusEnum statusEnum = PushStatusEnum.getByCode(status);
        return statusEnum != null ? statusEnum.getDesc() : "未知";
    }

    @Override
    public List<PushRecord> getAllRecords(Integer contentType, Integer pushStatus, String startDate,
            String endDate, String deviceName) {
        LambdaQueryWrapper<PushRecord> wrapper = buildQueryWrapper(contentType, pushStatus, startDate, endDate, deviceName);
        return pushRecordMapper.selectList(wrapper);
    }

    @Override
    public PushRecordResponse convertToResponse(PushRecord record) {
        PushRecordResponse response = new PushRecordResponse();
        response.setId(record.getId());
        response.setContentType(record.getContentType() == 1 ? "poster" : "video");
        response.setContentTitle(record.getContentName());
        response.setPushType(record.getPushType());
        response.setDeviceCount(record.getDeviceCount());
        response.setSuccessCount(record.getSuccessCount());
        response.setFailCount(record.getFailCount());
        response.setStatus(record.getPushStatus());
        response.setCreateTime(record.getPushTime());

        // 获取分组名称
        if (record.getGroupId() != null) {
            DeviceGroup group = deviceGroupMapper.selectById(record.getGroupId());
            if (group != null) {
                response.setGroupName(group.getGroupName());
            }
        }

        // 获取设备名称
        if (record.getTargetIds() != null && !record.getTargetIds().isEmpty()) {
            try {
                List<Long> deviceIds = JSONUtil.toList(record.getTargetIds(), Long.class);
                if (!deviceIds.isEmpty()) {
                    List<Device> devices = deviceMapper.selectBatchIds(deviceIds);
                    String deviceNames = devices.stream()
                            .map(Device::getDeviceName)
                            .collect(Collectors.joining("、"));
                    response.setDeviceNames(deviceNames);
                }
            } catch (Exception e) {
                // ignore
            }
        }

        // 获取操作人名称
        if (record.getPushBy() != null) {
            User user = userMapper.selectById(record.getPushBy());
            if (user != null) {
                response.setOperatorName(user.getRealName());
            }
        }

        return response;
    }
}
