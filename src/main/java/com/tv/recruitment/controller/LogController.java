package com.tv.recruitment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tv.recruitment.common.result.Result;
import com.tv.recruitment.dto.response.OperationTypeResponse;
import com.tv.recruitment.entity.OperationLog;
import com.tv.recruitment.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 操作日志控制器
 *
 * @author tv_recru
 */
@Tag(name = "操作日志")
@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    @Operation(summary = "分页查询日志")
    @GetMapping
    public Result<Page<OperationLog>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Page<OperationLog> result = logService.page(pageNum, pageSize, userName, operationType, startDate, endDate);
        return Result.success(result);
    }

    @Operation(summary = "获取操作类型列表")
    @GetMapping("/types")
    public Result<List<OperationTypeResponse>> getTypes() {
        return Result.success(logService.getTypes());
    }

    @Operation(summary = "清空日志")
    @DeleteMapping
    public Result<Void> clear() {
        logService.clear();
        return Result.success();
    }
}
