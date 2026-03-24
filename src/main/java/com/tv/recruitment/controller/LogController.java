package com.tv.recruitment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tv.recruitment.common.result.Result;
import com.tv.recruitment.dto.response.OperationTypeResponse;
import com.tv.recruitment.entity.OperationLog;
import com.tv.recruitment.mapper.OperationLogMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 操作日志控制器
 */
@Tag(name = "操作日志")
@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
public class LogController {

    private final OperationLogMapper operationLogMapper;

    @Operation(summary = "分页查询日志")
    @GetMapping
    public Result<Page<OperationLog>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String operationType) {
        Page<OperationLog> page = new Page<>(pageNum, pageSize);
        Page<OperationLog> result = operationLogMapper.selectPage(page,
                new LambdaQueryWrapper<OperationLog>()
                        .like(userName != null, OperationLog::getUserName, userName)
                        .eq(operationType != null, OperationLog::getOperationType, operationType)
                        .orderByDesc(OperationLog::getOperationTime));
        return Result.success(result);
    }

    @Operation(summary = "获取操作类型列表")
    @GetMapping("/types")
    public Result<List<OperationTypeResponse>> getTypes() {
        return Result.success(List.of(
                new OperationTypeResponse("LOGIN", "用户登录"),
                new OperationTypeResponse("LOGOUT", "用户退出"),
                new OperationTypeResponse("CREATE_JOB", "新增职位"),
                new OperationTypeResponse("UPDATE_JOB", "编辑职位"),
                new OperationTypeResponse("DELETE_JOB", "删除职位"),
                new OperationTypeResponse("CREATE_POSTER", "生成海报"),
                new OperationTypeResponse("PUSH_CONTENT", "推送内容")
        ));
    }

    @Operation(summary = "清空日志")
    @DeleteMapping
    public Result<Void> clear() {
        operationLogMapper.delete(null);
        return Result.success();
    }
}