package com.tv.recruitment.controller;

import com.tv.recruitment.common.result.Result;
import com.tv.recruitment.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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

    @Operation(summary = "获取推送统计")
    @GetMapping("/push")
    public Result<Map<String, Object>> getPushStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "week") String type) {
        return Result.success(statisticsService.getPushStatistics(startDate, endDate, type));
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

    @Operation(summary = "导出统计数据")
    @GetMapping("/export")
    public void exportStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            jakarta.servlet.http.HttpServletResponse response) {
        statisticsService.exportStatistics(startDate, endDate, response);
    }
}