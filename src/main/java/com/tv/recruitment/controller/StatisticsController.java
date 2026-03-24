package com.tv.recruitment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tv.recruitment.common.result.Result;
import com.tv.recruitment.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 统计控制器
 */
@Tag(name = "数据统计")
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Operation(summary = "获取推送统计概览")
    @GetMapping("/push")
    public Result<Map<String, Object>> getPushStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "week") String type) {
        return Result.success(statisticsService.getPushStatistics(startDate, endDate, type));
    }

    @Operation(summary = "获取推送记录明细列表")
    @GetMapping("/push/records")
    public Result<Page<Map<String, Object>>> getPushRecordList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) Integer contentType,
            @RequestParam(required = false) Integer pushStatus) {
        return Result.success(statisticsService.getPushRecordList(
                pageNum, pageSize, startDate, endDate, deviceId, contentType, pushStatus));
    }

    @Operation(summary = "获取设备状态统计")
    @GetMapping("/device/status")
    public Result<Map<String, Object>> getDeviceStatusStatistics(
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(statisticsService.getDeviceStatusStatistics(deviceId, startDate, endDate));
    }

    @Operation(summary = "获取设备统计")
    @GetMapping("/device")
    public Result<Map<String, Object>> getDeviceStatistics() {
        return Result.success(statisticsService.getDeviceStatistics());
    }

    @Operation(summary = "获取内容统计")
    @GetMapping("/content")
    public Result<Map<String, Object>> getContentStatistics() {
        return Result.success(statisticsService.getContentStatistics());
    }

    @Operation(summary = "导出推送记录Excel")
    @GetMapping("/push/export")
    public void exportPushRecords(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) Integer contentType,
            @RequestParam(required = false) Integer pushStatus,
            HttpServletResponse response) {
        statisticsService.exportPushRecords(startDate, endDate, deviceId, contentType, pushStatus, response);
    }

    @Operation(summary = "导出统计报表")
    @GetMapping("/export")
    public void exportStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletResponse response) {
        statisticsService.exportStatistics(startDate, endDate, response);
    }
}