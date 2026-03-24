package com.tv.recruitment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tv.recruitment.common.annotation.Log;
import com.tv.recruitment.common.result.Result;
import com.tv.recruitment.entity.Job;
import com.tv.recruitment.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 职位控制器
 */
@Tag(name = "职位管理")
@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @Operation(summary = "分页查询职位")
    @GetMapping
    public Result<Page<Job>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String jobName,
            @RequestParam(required = false) String workAddress,
            @RequestParam(required = false) Integer status) {
        return Result.success(jobService.page(pageNum, pageSize, jobName, workAddress, status));
    }

    @Operation(summary = "获取职位详情")
    @GetMapping("/{id}")
    public Result<Job> getById(@PathVariable Long id) {
        return Result.success(jobService.getById(id));
    }

    @Operation(summary = "新增职位")
    @PostMapping
    @Log(type = "CREATE", desc = "新增职位")
    public Result<Void> save(@RequestBody Job job) {
        jobService.save(job);
        return Result.success();
    }

    @Operation(summary = "编辑职位")
    @PutMapping("/{id}")
    @Log(type = "UPDATE", desc = "编辑职位")
    public Result<Void> update(@PathVariable Long id, @RequestBody Job job) {
        job.setId(id);
        jobService.updateById(job);
        return Result.success();
    }

    @Operation(summary = "删除职位")
    @DeleteMapping("/{id}")
    @Log(type = "DELETE", desc = "删除职位")
    public Result<Void> delete(@PathVariable Long id) {
        jobService.removeById(id);
        return Result.success();
    }

    @Operation(summary = "更改状态")
    @PutMapping("/{id}/status")
    @Log(type = "UPDATE", desc = "更改职位状态")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        jobService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "批量更改状态")
    @PutMapping("/batch/status")
    @Log(type = "UPDATE", desc = "批量更改职位状态")
    public Result<Void> batchUpdateStatus(@RequestBody List<Long> ids, @RequestParam Integer status) {
        jobService.batchUpdateStatus(ids, status);
        return Result.success();
    }
}