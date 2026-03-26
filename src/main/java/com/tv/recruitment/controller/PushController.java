package com.tv.recruitment.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tv.recruitment.common.annotation.Log;
import com.tv.recruitment.common.enums.PushStatusEnum;
import com.tv.recruitment.common.result.Result;
import com.tv.recruitment.common.utils.CsvExportUtils;
import com.tv.recruitment.common.utils.FileDownloadUtils;
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
import com.tv.recruitment.websocket.WebSocketHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 推送控制器
 */
@Slf4j
@Tag(name = "推送管理")
@RestController
@RequestMapping("/push")
@RequiredArgsConstructor
public class PushController {

    private final PushRecordMapper pushRecordMapper;
    private final WebSocketHandler webSocketHandler;
    private final PosterMapper posterMapper;
    private final VideoMapper videoMapper;
    private final DeviceMapper deviceMapper;
    private final DeviceGroupMapper deviceGroupMapper;
    private final UserMapper userMapper;

    @Operation(summary = "推送海报")
    @PostMapping("/poster")
    @Log(type = "PUSH", desc = "推送海报")
    public Result<Long> pushPoster(@RequestBody PushRequest request) {
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

        return Result.success(record.getId());
    }

    @Operation(summary = "推送视频")
    @PostMapping("/video")
    @Log(type = "PUSH", desc = "推送视频")
    public Result<Long> pushVideo(@RequestBody PushRequest request) {
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

        return Result.success(record.getId());
    }

    @Operation(summary = "设备控制")
    @PostMapping("/control")
    @Log(type = "PUSH", desc = "设备控制")
    public Result<Void> control(@RequestBody ControlRequest request) {
        for (Long deviceId : request.getDeviceIds()) {
            Device device = deviceMapper.selectById(deviceId);
            if (device != null) {
                webSocketHandler.sendControl(device.getDeviceCode(), request.getAction());
            }
        }
        return Result.success();
    }

    @Operation(summary = "获取推送记录")
    @GetMapping("/records")
    public Result<Page<PushRecordResponse>> getRecords(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer contentType,
            @RequestParam(required = false) Integer pushStatus,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String deviceName) {
        Page<PushRecord> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<PushRecord> wrapper = buildQueryWrapper(contentType, pushStatus, startDate, endDate, deviceName);
        Page<PushRecord> result = pushRecordMapper.selectPage(page, wrapper);

        // 转换为响应对象
        Page<PushRecordResponse> responsePage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<PushRecordResponse> records = result.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        responsePage.setRecords(records);

        return Result.success(responsePage);
    }

    @Operation(summary = "获取推送记录详情")
    @GetMapping("/records/{id}")
    public Result<PushRecordResponse> getRecordDetail(@PathVariable Long id) {
        PushRecord record = pushRecordMapper.selectById(id);
        if (record == null) {
            return Result.success(null);
        }
        return Result.success(convertToResponse(record));
    }

    @Operation(summary = "导出推送记录")
    @GetMapping("/records/export")
    public void exportRecords(
            @RequestParam(required = false) Integer contentType,
            @RequestParam(required = false) Integer pushStatus,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String deviceName,
            HttpServletResponse response) {
        try {
            // 构建查询条件
            LambdaQueryWrapper<PushRecord> wrapper = buildQueryWrapper(contentType, pushStatus, startDate, endDate, deviceName);
            List<PushRecord> records = pushRecordMapper.selectList(wrapper);

            // 生成CSV内容
            StringBuilder csv = new StringBuilder();
            csv.append("ID,内容名称,内容类型,推送类型,目标设备,设备数量,成功数量,失败数量,推送状态,操作人,推送时间\n");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (PushRecord record : records) {
                PushRecordResponse resp = convertToResponse(record);
                csv.append(resp.getId()).append(",");
                csv.append(CsvExportUtils.escapeCsv(resp.getContentTitle())).append(",");
                csv.append("poster".equals(resp.getContentType()) ? "海报" : "视频").append(",");
                csv.append(getPushTypeText(resp.getPushType())).append(",");
                csv.append(CsvExportUtils.escapeCsv(resp.getDeviceNames())).append(",");
                csv.append(resp.getDeviceCount() != null ? resp.getDeviceCount() : 0).append(",");
                csv.append(resp.getSuccessCount() != null ? resp.getSuccessCount() : 0).append(",");
                csv.append(resp.getFailCount() != null ? resp.getFailCount() : 0).append(",");
                csv.append(getStatusText(resp.getStatus())).append(",");
                csv.append(CsvExportUtils.escapeCsv(resp.getOperatorName())).append(",");
                csv.append(resp.getCreateTime() != null ? resp.getCreateTime().format(formatter) : "").append("\n");
            }

            // 设置响应头
            String fileName = "推送记录_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            CsvExportUtils.setCsvResponseHeaders(response, fileName);

            // 写入BOM和CSV内容
            OutputStream os = response.getOutputStream();
            CsvExportUtils.writeBom(os);
            os.write(csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            os.flush();
        } catch (IOException e) {
            FileDownloadUtils.writeErrorResponse(response, 500, "导出失败: " + e.getMessage());
        }
    }

    @Operation(summary = "批量推送")
    @PostMapping("/multiple")
    @Log(type = "PUSH", desc = "批量推送内容")
    public Result<Long> pushMultiple(@RequestBody PushRequest request) {
        // 转换contentType: poster -> 1, video -> 2
        int contentTypeInt = "video".equals(request.getContentType()) ? 2 : 1;
        Long contentId = request.getContentIds() != null && !request.getContentIds().isEmpty()
            ? request.getContentIds().get(0) : request.getContentId();

        // 获取内容名称
        String contentName = null;
        if (contentId != null) {
            if (contentTypeInt == 1) {
                Poster poster = posterMapper.selectById(contentId);
                if (poster != null) {
                    contentName = poster.getPosterName();
                }
            } else {
                Video video = videoMapper.selectById(contentId);
                if (video != null) {
                    contentName = video.getVideoName();
                }
            }
        }

        // 处理分组推送
        List<Long> targetIds = resolveTargetIds(request);

        if (targetIds.isEmpty()) {
            return Result.error("请选择推送目标");
        }

        PushRecord record = new PushRecord();
        record.setContentType(contentTypeInt);
        record.setContentId(contentId);
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

        return Result.success(record.getId());
    }

    @Operation(summary = "获取分组下的设备ID列表")
    @GetMapping("/devices/byGroup/{groupId}")
    public Result<List<Long>> getDeviceIdsByGroup(@PathVariable Long groupId) {
        List<Device> devices = deviceMapper.selectList(
                new LambdaQueryWrapper<Device>()
                        .eq(Device::getGroupId, groupId)
                        .eq(Device::getStatus, 1)
        );
        List<Long> deviceIds = devices.stream()
                .map(Device::getId)
                .collect(Collectors.toList());
        return Result.success(deviceIds);
    }

    @Operation(summary = "获取分组信息（含设备统计）")
    @GetMapping("/groups")
    public Result<List<Map<String, Object>>> getPushGroups() {
        List<DeviceGroup> groups = deviceGroupMapper.selectList(
                new LambdaQueryWrapper<DeviceGroup>().orderByAsc(DeviceGroup::getCreateTime)
        );

        List<Map<String, Object>> result = new ArrayList<>();
        for (DeviceGroup group : groups) {
            Map<String, Object> item = new java.util.HashMap<>();
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

        return Result.success(result);
    }

    /**
     * 解析目标设备ID列表
     * 支持按分组推送和按设备ID推送
     */
    private List<Long> resolveTargetIds(PushRequest request) {
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

    /**
     * 构建查询条件（供列表和导出共用）
     */
    private LambdaQueryWrapper<PushRecord> buildQueryWrapper(Integer contentType, Integer pushStatus,
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

    /**
     * 执行推送
     */
    private void executePush(PushRecord record, List<Long> targetIds) {
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

    /**
     * 确定推送类型
     * 1-单台推送 2-多台推送 3-分组推送
     */
    private Integer determinePushType(PushRequest request) {
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

    /**
     * 获取推送类型文本
     */
    private String getPushTypeText(Integer pushType) {
        if (pushType == null) return "未知";
        switch (pushType) {
            case 1: return "单台推送";
            case 2: return "多台推送";
            case 3: return "分组推送";
            default: return "未知";
        }
    }

    /**
     * 获取状态文本
     */
    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        PushStatusEnum statusEnum = PushStatusEnum.getByCode(status);
        return statusEnum != null ? statusEnum.getDesc() : "未知";
    }

    /**
     * 转换为响应对象
     */
    private PushRecordResponse convertToResponse(PushRecord record) {
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