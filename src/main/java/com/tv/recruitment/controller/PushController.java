package com.tv.recruitment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tv.recruitment.common.annotation.Log;
import com.tv.recruitment.common.result.Result;
import com.tv.recruitment.common.utils.CsvExportUtils;
import com.tv.recruitment.common.utils.FileDownloadUtils;
import com.tv.recruitment.dto.request.ControlRequest;
import com.tv.recruitment.dto.request.PushRequest;
import com.tv.recruitment.dto.response.PushRecordResponse;
import com.tv.recruitment.entity.PushRecord;
import com.tv.recruitment.service.PushService;
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
import java.util.List;
import java.util.Map;

/**
 * 推送控制器
 */
@Slf4j
@Tag(name = "推送管理")
@RestController
@RequestMapping("/push")
@RequiredArgsConstructor
public class PushController {

    private final PushService pushService;

    @Operation(summary = "推送海报")
    @PostMapping("/poster")
    @Log(type = "PUSH", desc = "推送海报")
    public Result<Long> pushPoster(@RequestBody PushRequest request) {
        Long recordId = pushService.pushPoster(request);
        return Result.success(recordId);
    }

    @Operation(summary = "推送视频")
    @PostMapping("/video")
    @Log(type = "PUSH", desc = "推送视频")
    public Result<Long> pushVideo(@RequestBody PushRequest request) {
        Long recordId = pushService.pushVideo(request);
        return Result.success(recordId);
    }

    @Operation(summary = "设备控制")
    @PostMapping("/control")
    @Log(type = "PUSH", desc = "设备控制")
    public Result<Void> control(@RequestBody ControlRequest request) {
        pushService.control(request);
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
        Page<PushRecordResponse> page = pushService.getRecords(pageNum, pageSize, contentType,
                pushStatus, startDate, endDate, deviceName);
        return Result.success(page);
    }

    @Operation(summary = "获取推送记录详情")
    @GetMapping("/records/{id}")
    public Result<PushRecordResponse> getRecordDetail(@PathVariable Long id) {
        PushRecordResponse record = pushService.getRecordDetail(id);
        return Result.success(record);
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
            List<PushRecord> records = pushService.getAllRecords(contentType, pushStatus, startDate, endDate, deviceName);

            // 生成CSV内容
            StringBuilder csv = new StringBuilder();
            csv.append("ID,内容名称,内容类型,推送类型,目标设备,设备数量,成功数量,失败数量,推送状态,操作人,推送时间\n");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (PushRecord record : records) {
                PushRecordResponse resp = pushService.convertToResponse(record);
                csv.append(resp.getId()).append(",");
                csv.append(CsvExportUtils.escapeCsv(resp.getContentTitle())).append(",");
                csv.append("poster".equals(resp.getContentType()) ? "海报" : "视频").append(",");
                csv.append(pushService.getPushTypeText(resp.getPushType())).append(",");
                csv.append(CsvExportUtils.escapeCsv(resp.getDeviceNames())).append(",");
                csv.append(resp.getDeviceCount() != null ? resp.getDeviceCount() : 0).append(",");
                csv.append(resp.getSuccessCount() != null ? resp.getSuccessCount() : 0).append(",");
                csv.append(resp.getFailCount() != null ? resp.getFailCount() : 0).append(",");
                csv.append(pushService.getStatusText(resp.getStatus())).append(",");
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
        Long recordId = pushService.pushMultiple(request);
        return Result.success(recordId);
    }

    @Operation(summary = "获取分组下的设备ID列表")
    @GetMapping("/devices/byGroup/{groupId}")
    public Result<List<Long>> getDeviceIdsByGroup(@PathVariable Long groupId) {
        List<Long> deviceIds = pushService.getDeviceIdsByGroup(groupId);
        return Result.success(deviceIds);
    }

    @Operation(summary = "获取分组信息（含设备统计）")
    @GetMapping("/groups")
    public Result<List<Map<String, Object>>> getPushGroups() {
        List<Map<String, Object>> groups = pushService.getPushGroups();
        return Result.success(groups);
    }
}
