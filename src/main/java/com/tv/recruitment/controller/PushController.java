package com.tv.recruitment.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tv.recruitment.common.result.Result;
import com.tv.recruitment.entity.PushRecord;
import com.tv.recruitment.mapper.PushRecordMapper;
import com.tv.recruitment.websocket.WebSocketHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 推送控制器
 */
@Tag(name = "推送管理")
@RestController
@RequestMapping("/push")
@RequiredArgsConstructor
public class PushController {

    private final PushRecordMapper pushRecordMapper;
    private final WebSocketHandler webSocketHandler;

    @Operation(summary = "推送海报")
    @PostMapping("/poster")
    public Result<Long> pushPoster(@RequestBody PushRequest request) {
        // 创建推送记录
        PushRecord record = new PushRecord();
        record.setContentType(1);
        record.setContentId(request.getPosterId());
        record.setPushType(request.getPushType());
        record.setTargetIds(JSONUtil.toJsonStr(request.getTargetIds()));
        record.setPlayRule(JSONUtil.toJsonStr(request.getPlayRule()));
        record.setPushStatus(0);
        pushRecordMapper.insert(record);

        // 执行推送
        executePush(record, request);

        return Result.success(record.getId());
    }

    @Operation(summary = "推送视频")
    @PostMapping("/video")
    public Result<Long> pushVideo(@RequestBody PushRequest request) {
        PushRecord record = new PushRecord();
        record.setContentType(2);
        record.setContentId(request.getVideoId());
        record.setPushType(request.getPushType());
        record.setTargetIds(JSONUtil.toJsonStr(request.getTargetIds()));
        record.setPlayRule(JSONUtil.toJsonStr(request.getPlayRule()));
        record.setPushStatus(0);
        pushRecordMapper.insert(record);

        executePush(record, request);

        return Result.success(record.getId());
    }

    @Operation(summary = "设备控制")
    @PostMapping("/control")
    public Result<Void> control(@RequestBody ControlRequest request) {
        for (Long deviceId : request.getDeviceIds()) {
            // TODO: 根据设备ID获取设备编码
            // webSocketHandler.sendControl(deviceCode, request.getAction());
        }
        return Result.success();
    }

    @Operation(summary = "获取推送记录")
    @GetMapping("/records")
    public Result<Page<PushRecord>> getRecords(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer contentType,
            @RequestParam(required = false) Integer pushStatus) {
        Page<PushRecord> page = new Page<>(pageNum, pageSize);
        Page<PushRecord> result = pushRecordMapper.selectPage(page,
                new LambdaQueryWrapper<PushRecord>()
                        .eq(contentType != null, PushRecord::getContentType, contentType)
                        .eq(pushStatus != null, PushRecord::getPushStatus, pushStatus)
                        .orderByDesc(PushRecord::getPushTime));
        return Result.success(result);
    }

    @Operation(summary = "获取推送记录详情")
    @GetMapping("/records/{id}")
    public Result<PushRecord> getRecordDetail(@PathVariable Long id) {
        return Result.success(pushRecordMapper.selectById(id));
    }

    @Operation(summary = "批量推送")
    @PostMapping("/multiple")
    public Result<Long> pushMultiple(@RequestBody PushRequest request) {
        PushRecord record = new PushRecord();
        record.setContentType(request.getContentType());
        record.setContentId(request.getContentId());
        record.setPushType(request.getPushType());
        record.setTargetIds(JSONUtil.toJsonStr(request.getTargetIds()));
        record.setPlayRule(JSONUtil.toJsonStr(request.getPlayRule()));
        record.setDeviceCount(request.getTargetIds().size());
        record.setPushStatus(0);
        pushRecordMapper.insert(record);

        executePush(record, request);

        return Result.success(record.getId());
    }

    private void executePush(PushRecord record, PushRequest request) {
        List<Long> targetIds = request.getTargetIds();
        for (Long deviceId : targetIds) {
            // TODO: 获取设备编码并推送
            // String deviceCode = ...;
            // webSocketHandler.pushContent(deviceCode, contentType, contentUrl, playRule);
        }
    }

    // 内部类
    @lombok.Data
    public static class PushRequest {
        private Long posterId;
        private Long videoId;
        private Integer contentType;
        private Long contentId;
        private Integer pushType;
        private List<Long> targetIds;
        private Long groupId;
        private Object playRule;
    }

    @lombok.Data
    public static class ControlRequest {
        private List<Long> deviceIds;
        private String action;
    }
}