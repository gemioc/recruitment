package com.tv.recruitment.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tv.recruitment.common.utils.CsvExportUtils;
import com.tv.recruitment.entity.*;
import com.tv.recruitment.mapper.*;
import com.tv.recruitment.service.StatisticsService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final DeviceMapper deviceMapper;
    private final JobMapper jobMapper;
    private final PosterMapper posterMapper;
    private final VideoMapper videoMapper;
    private final PushRecordMapper pushRecordMapper;
    private final UserMapper userMapper;

    @Override
    public Map<String, Object> getPushStatistics(String startDate, String endDate, String type) {
        Map<String, Object> result = new HashMap<>();

        // 计算时间范围
        LocalDateTime start = calculateStartDate(type);
        LocalDateTime end = LocalDateTime.now();

        if (startDate != null && !startDate.isEmpty()) {
            start = LocalDate.parse(startDate).atStartOfDay();
        }
        if (endDate != null && !endDate.isEmpty()) {
            end = LocalDate.parse(endDate).atTime(23, 59, 59);
        }

        // 总推送次数
        Long total = pushRecordMapper.selectCount(
                new LambdaQueryWrapper<PushRecord>()
                        .ge(PushRecord::getPushTime, start)
                        .le(PushRecord::getPushTime, end)
        );
        result.put("total", total);

        // 成功次数
        Long success = pushRecordMapper.selectCount(
                new LambdaQueryWrapper<PushRecord>()
                        .eq(PushRecord::getPushStatus, 1)
                        .ge(PushRecord::getPushTime, start)
                        .le(PushRecord::getPushTime, end)
        );
        result.put("success", success);

        // 失败次数
        Long fail = pushRecordMapper.selectCount(
                new LambdaQueryWrapper<PushRecord>()
                        .eq(PushRecord::getPushStatus, 2)
                        .ge(PushRecord::getPushTime, start)
                        .le(PushRecord::getPushTime, end)
        );
        result.put("fail", fail);

        // 今日推送
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        Long today = pushRecordMapper.selectCount(
                new LambdaQueryWrapper<PushRecord>()
                        .ge(PushRecord::getPushTime, todayStart)
        );
        result.put("today", today);

        // 趋势数据
        List<Map<String, Object>> trend = generateTrendData(start, end);
        result.put("trend", trend);

        // 内容类型分布
        List<Map<String, Object>> typeDistribution = new ArrayList<>();

        Long posterCount = pushRecordMapper.selectCount(
                new LambdaQueryWrapper<PushRecord>()
                        .eq(PushRecord::getContentType, 1)
                        .ge(PushRecord::getPushTime, start)
                        .le(PushRecord::getPushTime, end)
        );
        Map<String, Object> posterMap = new HashMap<>();
        posterMap.put("type", "poster");
        posterMap.put("count", posterCount);
        typeDistribution.add(posterMap);

        Long videoCount = pushRecordMapper.selectCount(
                new LambdaQueryWrapper<PushRecord>()
                        .eq(PushRecord::getContentType, 2)
                        .ge(PushRecord::getPushTime, start)
                        .le(PushRecord::getPushTime, end)
        );
        Map<String, Object> videoMap = new HashMap<>();
        videoMap.put("type", "video");
        videoMap.put("count", videoCount);
        typeDistribution.add(videoMap);

        result.put("typeDistribution", typeDistribution);

        // 时段分布：按小时统计推送次数
        int[] hourDistribution = generateHourDistribution(start, end);
        result.put("hourDistribution", hourDistribution);

        // 设备推送排行：统计每个设备被推送的次数
        List<Map<String, Object>> deviceRank = generateDeviceRank(start, end, 10);
        result.put("deviceRank", deviceRank);

        return result;
    }

    @Override
    public Page<Map<String, Object>> getPushRecordList(Integer pageNum, Integer pageSize,
            String startDate, String endDate, Long deviceId, Integer contentType, Integer pushStatus) {

        // 构建查询条件
        LambdaQueryWrapper<PushRecord> wrapper = new LambdaQueryWrapper<>();

        // 时间范围
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(PushRecord::getPushTime, LocalDate.parse(startDate).atStartOfDay());
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(PushRecord::getPushTime, LocalDate.parse(endDate).atTime(23, 59, 59));
        }

        // 内容类型
        if (contentType != null) {
            wrapper.eq(PushRecord::getContentType, contentType);
        }

        // 推送状态
        if (pushStatus != null) {
            wrapper.eq(PushRecord::getPushStatus, pushStatus);
        }

        // 设备ID筛选（需要解析JSON）
        wrapper.orderByDesc(PushRecord::getPushTime);

        Page<PushRecord> page = pushRecordMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        // 转换为详细信息的Map
        Page<Map<String, Object>> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<Map<String, Object>> records = new ArrayList<>();

        // 批量获取设备信息
        Map<Long, Device> deviceMap = new HashMap<>();
        Map<Long, User> userMap = new HashMap<>();

        for (PushRecord record : page.getRecords()) {
            // 解析目标设备ID
            if (record.getTargetIds() != null && !record.getTargetIds().isEmpty()) {
                try {
                    List<Long> deviceIds = JSONUtil.toList(record.getTargetIds(), Long.class);
                    for (Long id : deviceIds) {
                        if (!deviceMap.containsKey(id)) {
                            Device device = deviceMapper.selectById(id);
                            if (device != null) {
                                deviceMap.put(id, device);
                            }
                        }
                    }
                } catch (Exception e) {
                    // ignore
                }
            }

            // 获取操作人信息
            if (record.getPushBy() != null && !userMap.containsKey(record.getPushBy())) {
                User user = userMapper.selectById(record.getPushBy());
                if (user != null) {
                    userMap.put(record.getPushBy(), user);
                }
            }
        }

        // 设备ID筛选（在内存中过滤）
        List<PushRecord> filteredRecords = page.getRecords();
        if (deviceId != null) {
            filteredRecords = filteredRecords.stream()
                    .filter(r -> {
                        if (r.getTargetIds() == null) return false;
                        try {
                            List<Long> deviceIds = JSONUtil.toList(r.getTargetIds(), Long.class);
                            return deviceIds.contains(deviceId);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
            resultPage.setTotal(filteredRecords.size());
        }

        for (PushRecord record : filteredRecords) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", record.getId());
            item.put("pushTime", record.getPushTime());
            item.put("contentType", record.getContentType() == 1 ? "海报" : "视频");
            item.put("contentName", record.getContentName());
            item.put("pushStatus", record.getPushStatus());
            item.put("pushStatusText", getPushStatusText(record.getPushStatus()));
            item.put("deviceCount", record.getDeviceCount());
            item.put("successCount", record.getSuccessCount());
            item.put("failCount", record.getFailCount());

            // 推送对象（设备名称列表）
            if (record.getTargetIds() != null && !record.getTargetIds().isEmpty()) {
                try {
                    List<Long> deviceIds = JSONUtil.toList(record.getTargetIds(), Long.class);
                    String deviceNames = deviceIds.stream()
                            .map(id -> deviceMap.containsKey(id) ? deviceMap.get(id).getDeviceName() : "未知设备")
                            .collect(Collectors.joining("、"));
                    item.put("targetDevices", deviceNames);
                } catch (Exception e) {
                    item.put("targetDevices", "");
                }
            } else {
                item.put("targetDevices", "");
            }

            // 操作人
            if (record.getPushBy() != null && userMap.containsKey(record.getPushBy())) {
                item.put("operatorName", userMap.get(record.getPushBy()).getRealName());
            } else {
                item.put("operatorName", "");
            }

            records.add(item);
        }

        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    public Map<String, Object> getDeviceStatusStatistics(Long deviceId, String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();

        // 时间范围默认最近7天
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();

        if (startDate != null && !startDate.isEmpty()) {
            start = LocalDate.parse(startDate).atStartOfDay();
        }
        if (endDate != null && !endDate.isEmpty()) {
            end = LocalDate.parse(endDate).atTime(23, 59, 59);
        }

        // 设备列表
        List<Device> devices;
        if (deviceId != null) {
            Device device = deviceMapper.selectById(deviceId);
            devices = device != null ? List.of(device) : new ArrayList<>();
        } else {
            devices = deviceMapper.selectList(null);
        }

        // 在线设备数
        long onlineCount = devices.stream().filter(d -> d.getOnlineStatus() != null && d.getOnlineStatus() == 1).count();
        result.put("onlineCount", onlineCount);
        result.put("offlineCount", devices.size() - onlineCount);
        result.put("totalDevices", devices.size());

        // 设备状态详情列表
        List<Map<String, Object>> deviceStatusList = new ArrayList<>();
        for (Device device : devices) {
            Map<String, Object> deviceStatus = new HashMap<>();
            deviceStatus.put("id", device.getId());
            deviceStatus.put("name", device.getDeviceName());
            deviceStatus.put("deviceCode", device.getDeviceCode());
            deviceStatus.put("onlineStatus", device.getOnlineStatus());
            deviceStatus.put("onlineStatusText", device.getOnlineStatus() != null && device.getOnlineStatus() == 1 ? "在线" : "离线");
            deviceStatus.put("lastOnlineTime", device.getLastOnlineTime());
            deviceStatus.put("lastHeartbeat", device.getLastHeartbeat());

            // 在线时长（累计 + 当前在线时长）
            long totalOnlineDuration = device.getTotalOnlineDuration() != null ? device.getTotalOnlineDuration() : 0;
            if (device.getOnlineStatus() != null && device.getOnlineStatus() == 1 && device.getLastOnlineTime() != null) {
                // 设备当前在线，加上当前在线时长
                totalOnlineDuration += java.time.Duration.between(device.getLastOnlineTime(), LocalDateTime.now()).getSeconds();
            }
            deviceStatus.put("totalOnlineDuration", totalOnlineDuration);
            deviceStatus.put("totalOnlineDurationText", formatDuration(totalOnlineDuration));

            // 离线次数
            deviceStatus.put("offlineCount", device.getOfflineCount() != null ? device.getOfflineCount() : 0);

            // 统计该设备的推送次数
            Long pushCount = countDevicePushCount(device.getId(), start, end);
            deviceStatus.put("pushCount", pushCount);

            // 当前播放内容
            if (device.getCurrentContentType() != null) {
                deviceStatus.put("currentContentType", device.getCurrentContentType() == 1 ? "海报" : "视频");
                if (device.getCurrentContentId() != null) {
                    String contentName = getContentName(device.getCurrentContentType(), device.getCurrentContentId());
                    deviceStatus.put("currentContentName", contentName);
                }
                deviceStatus.put("contentStartTime", device.getContentStartTime());
                // 计算当前内容展示时长
                if (device.getContentStartTime() != null) {
                    long displayDuration = java.time.Duration.between(device.getContentStartTime(), LocalDateTime.now()).getSeconds();
                    deviceStatus.put("contentDisplayDuration", displayDuration);
                    deviceStatus.put("contentDisplayDurationText", formatDuration(displayDuration));
                }
            }

            deviceStatusList.add(deviceStatus);
        }
        result.put("deviceStatusList", deviceStatusList);

        // 按日期统计推送次数趋势
        List<Map<String, Object>> dailyPushTrend = generateDailyPushTrend(start, end, deviceId);
        result.put("dailyPushTrend", dailyPushTrend);

        return result;
    }

    @Override
    public Map<String, Object> getDeviceStatistics() {
        Map<String, Object> result = new HashMap<>();

        // 设备总数
        Long total = deviceMapper.selectCount(null);
        result.put("total", total);

        // 在线设备
        Long online = deviceMapper.selectCount(
                new LambdaQueryWrapper<Device>().eq(Device::getOnlineStatus, 1)
        );
        result.put("online", online);

        // 离线设备
        Long offline = total - online;
        result.put("offline", offline);

        // 状态分布
        List<Map<String, Object>> statusDistribution = new ArrayList<>();

        Map<String, Object> onlineMap = new HashMap<>();
        onlineMap.put("name", "在线");
        onlineMap.put("count", online);
        statusDistribution.add(onlineMap);

        Map<String, Object> offlineMap = new HashMap<>();
        offlineMap.put("name", "离线");
        offlineMap.put("count", offline);
        statusDistribution.add(offlineMap);

        result.put("statusDistribution", statusDistribution);

        return result;
    }

    @Override
    public Map<String, Object> getContentStatistics() {
        Map<String, Object> result = new HashMap<>();

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        // 职位统计
        Long jobCount = jobMapper.selectCount(
                new LambdaQueryWrapper<Job>().eq(Job::getStatus, 1)
        );
        result.put("jobCount", jobCount);

        // 今日新增职位
        Long todayJobCount = jobMapper.selectCount(
                new LambdaQueryWrapper<Job>().ge(Job::getCreateTime, todayStart)
        );
        result.put("todayJobCount", todayJobCount);

        // 海报统计
        Long posterCount = posterMapper.selectCount(null);
        result.put("posterCount", posterCount);

        // 今日生成海报
        Long todayPosterCount = posterMapper.selectCount(
                new LambdaQueryWrapper<Poster>().ge(Poster::getCreateTime, todayStart)
        );
        result.put("todayPosterCount", todayPosterCount);

        // 视频统计
        Long videoCount = videoMapper.selectCount(null);
        result.put("videoCount", videoCount);

        return result;
    }

    @Override
    public void exportPushRecords(String startDate, String endDate, Long deviceId,
            Integer contentType, Integer pushStatus, HttpServletResponse response) {
        try {
            // 设置Excel响应头
            String fileName = "推送记录_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName + ".xlsx", "UTF-8"));

            // 查询数据（不分页，获取全部）
            LambdaQueryWrapper<PushRecord> wrapper = new LambdaQueryWrapper<>();

            if (startDate != null && !startDate.isEmpty()) {
                wrapper.ge(PushRecord::getPushTime, LocalDate.parse(startDate).atStartOfDay());
            }
            if (endDate != null && !endDate.isEmpty()) {
                wrapper.le(PushRecord::getPushTime, LocalDate.parse(endDate).atTime(23, 59, 59));
            }
            if (contentType != null) {
                wrapper.eq(PushRecord::getContentType, contentType);
            }
            if (pushStatus != null) {
                wrapper.eq(PushRecord::getPushStatus, pushStatus);
            }
            wrapper.orderByDesc(PushRecord::getPushTime);

            List<PushRecord> records = pushRecordMapper.selectList(wrapper);

            // 创建Excel工作簿
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("推送记录");

                // 创建标题样式
                CellStyle headerStyle = workbook.createCellStyle();
                headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                headerStyle.setAlignment(HorizontalAlignment.CENTER);
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);

                // 创建表头
                String[] headers = {"ID", "推送时间", "内容类型", "内容名称", "推送对象", "设备数量", "成功数", "失败数", "推送状态", "操作人"};
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                // 创建日期样式
                CellStyle dateStyle = workbook.createCellStyle();
                CreationHelper createHelper = workbook.getCreationHelper();
                dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                // 填充数据
                int rowNum = 1;
                Map<Long, Device> deviceCache = new HashMap<>();
                Map<Long, User> userCache = new HashMap<>();

                for (PushRecord record : records) {
                    // 设备ID筛选
                    if (deviceId != null) {
                        if (record.getTargetIds() == null) continue;
                        try {
                            List<Long> deviceIds = JSONUtil.toList(record.getTargetIds(), Long.class);
                            if (!deviceIds.contains(deviceId)) continue;
                        } catch (Exception e) {
                            continue;
                        }
                    }

                    Row row = sheet.createRow(rowNum++);

                    row.createCell(0).setCellValue(record.getId());
                    row.createCell(1).setCellValue(record.getPushTime() != null ? record.getPushTime().format(formatter) : "");

                    // 内容类型
                    String contentTypeText = record.getContentType() == 1 ? "海报" : "视频";
                    row.createCell(2).setCellValue(contentTypeText);

                    row.createCell(3).setCellValue(record.getContentName() != null ? record.getContentName() : "");

                    // 推送对象
                    String targetDevices = "";
                    if (record.getTargetIds() != null && !record.getTargetIds().isEmpty()) {
                        try {
                            List<Long> deviceIds = JSONUtil.toList(record.getTargetIds(), Long.class);
                            targetDevices = deviceIds.stream()
                                    .map(id -> {
                                        if (!deviceCache.containsKey(id)) {
                                            Device d = deviceMapper.selectById(id);
                                            if (d != null) deviceCache.put(id, d);
                                        }
                                        return deviceCache.containsKey(id) ? deviceCache.get(id).getDeviceName() : "未知";
                                    })
                                    .collect(Collectors.joining("、"));
                        } catch (Exception e) {
                            // ignore
                        }
                    }
                    row.createCell(4).setCellValue(targetDevices);

                    row.createCell(5).setCellValue(record.getDeviceCount() != null ? record.getDeviceCount() : 0);
                    row.createCell(6).setCellValue(record.getSuccessCount() != null ? record.getSuccessCount() : 0);
                    row.createCell(7).setCellValue(record.getFailCount() != null ? record.getFailCount() : 0);
                    row.createCell(8).setCellValue(getPushStatusText(record.getPushStatus()));

                    // 操作人
                    String operatorName = "";
                    if (record.getPushBy() != null) {
                        if (!userCache.containsKey(record.getPushBy())) {
                            User user = userMapper.selectById(record.getPushBy());
                            if (user != null) userCache.put(record.getPushBy(), user);
                        }
                        operatorName = userCache.containsKey(record.getPushBy()) ?
                                userCache.get(record.getPushBy()).getRealName() : "";
                    }
                    row.createCell(9).setCellValue(operatorName);
                }

                // 自动调整列宽
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                // 写入响应
                OutputStream out = response.getOutputStream();
                workbook.write(out);
                out.flush();
            }

        } catch (IOException e) {
            log.error("导出推送记录失败", e);
        }
    }

    @Override
    public void exportStatistics(String startDate, String endDate, HttpServletResponse response) {
        try {
            // 设置Excel响应头
            String fileName = "统计报表_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName + ".xlsx", "UTF-8"));

            LocalDateTime start = startDate != null && !startDate.isEmpty() ?
                    LocalDate.parse(startDate).atStartOfDay() : LocalDateTime.now().minusDays(7);
            LocalDateTime end = endDate != null && !endDate.isEmpty() ?
                    LocalDate.parse(endDate).atTime(23, 59, 59) : LocalDateTime.now();

            try (Workbook workbook = new XSSFWorkbook()) {
                // 创建样式
                CellStyle headerStyle = workbook.createCellStyle();
                headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                headerStyle.setAlignment(HorizontalAlignment.CENTER);
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);

                // Sheet1: 概览统计
                Sheet overviewSheet = workbook.createSheet("概览统计");
                createOverviewSheet(overviewSheet, headerStyle, start, end);

                // Sheet2: 推送趋势
                Sheet trendSheet = workbook.createSheet("推送趋势");
                createTrendSheet(trendSheet, headerStyle, start, end);

                // Sheet3: 设备排行
                Sheet rankSheet = workbook.createSheet("设备排行");
                createDeviceRankSheet(rankSheet, headerStyle, start, end);

                // 写入响应
                OutputStream out = response.getOutputStream();
                workbook.write(out);
                out.flush();
            }

        } catch (IOException e) {
            log.error("导出统计报表失败", e);
        }
    }

    private void createOverviewSheet(Sheet sheet, CellStyle headerStyle, LocalDateTime start, LocalDateTime end) {
        Row titleRow = sheet.createRow(0);
        titleRow.createCell(0).setCellValue("统计报表");
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

        Row timeRow = sheet.createRow(1);
        timeRow.createCell(0).setCellValue("统计时间：" + start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) +
                " 至 " + end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 3));

        int rowNum = 3;

        // 推送统计
        Row pushHeaderRow = sheet.createRow(rowNum++);
        pushHeaderRow.createCell(0).setCellValue("推送统计");
        sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 1));

        Long totalPush = pushRecordMapper.selectCount(
                new LambdaQueryWrapper<PushRecord>()
                        .ge(PushRecord::getPushTime, start)
                        .le(PushRecord::getPushTime, end)
        );
        Long successPush = pushRecordMapper.selectCount(
                new LambdaQueryWrapper<PushRecord>()
                        .eq(PushRecord::getPushStatus, 1)
                        .ge(PushRecord::getPushTime, start)
                        .le(PushRecord::getPushTime, end)
        );

        sheet.createRow(rowNum++).createCell(0).setCellValue("推送总次数：" + totalPush);
        sheet.createRow(rowNum++).createCell(0).setCellValue("推送成功：" + successPush);
        sheet.createRow(rowNum++).createCell(0).setCellValue("推送失败：" + (totalPush - successPush));
        sheet.createRow(rowNum++).createCell(0).setCellValue("成功率：" + (totalPush > 0 ? Math.round(successPush * 100.0 / totalPush) : 0) + "%");

        rowNum++;

        // 设备统计
        sheet.createRow(rowNum++).createCell(0).setCellValue("设备统计");
        Long totalDevices = deviceMapper.selectCount(null);
        Long onlineDevices = deviceMapper.selectCount(
                new LambdaQueryWrapper<Device>().eq(Device::getOnlineStatus, 1)
        );
        sheet.createRow(rowNum++).createCell(0).setCellValue("设备总数：" + totalDevices);
        sheet.createRow(rowNum++).createCell(0).setCellValue("在线设备：" + onlineDevices);
        sheet.createRow(rowNum++).createCell(0).setCellValue("离线设备：" + (totalDevices - onlineDevices));

        rowNum++;

        // 内容统计
        sheet.createRow(rowNum++).createCell(0).setCellValue("内容统计");
        sheet.createRow(rowNum++).createCell(0).setCellValue("职位数量：" + jobMapper.selectCount(null));
        sheet.createRow(rowNum++).createCell(0).setCellValue("海报数量：" + posterMapper.selectCount(null));
        sheet.createRow(rowNum++).createCell(0).setCellValue("视频数量：" + videoMapper.selectCount(null));

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createTrendSheet(Sheet sheet, CellStyle headerStyle, LocalDateTime start, LocalDateTime end) {
        String[] headers = {"日期", "推送次数", "海报推送", "视频推送", "成功次数"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        List<Map<String, Object>> trend = generateTrendData(start, end);
        int rowNum = 1;
        for (Map<String, Object> day : trend) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue((String) day.get("date"));
            row.createCell(1).setCellValue((Integer) day.get("total"));
            row.createCell(2).setCellValue((Integer) day.get("posterCount"));
            row.createCell(3).setCellValue((Integer) day.get("videoCount"));
            row.createCell(4).setCellValue((Integer) day.get("success"));
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createDeviceRankSheet(Sheet sheet, CellStyle headerStyle, LocalDateTime start, LocalDateTime end) {
        String[] headers = {"排名", "设备名称", "推送次数"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        List<Map<String, Object>> rank = generateDeviceRank(start, end, 20);
        int rowNum = 1;
        for (Map<String, Object> device : rank) {
            Row row = sheet.createRow(rowNum);
            row.createCell(0).setCellValue(rowNum);
            row.createCell(1).setCellValue((String) device.get("name"));
            row.createCell(2).setCellValue((Integer) device.get("count"));
            rowNum++;
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * 计算起始时间
     */
    private LocalDateTime calculateStartDate(String type) {
        if ("all".equals(type)) {
            // 全量统计，返回一个很早的时间点
            return LocalDateTime.of(2020, 1, 1, 0, 0);
        }
        LocalDateTime now = LocalDateTime.now();
        switch (type) {
            case "day":
                return now.toLocalDate().atStartOfDay();
            case "week":
                return now.minusDays(7);
            case "month":
                return now.minusDays(30);
            default:
                return now.minusDays(7);
        }
    }

    /**
     * 生成趋势数据
     */
    private List<Map<String, Object>> generateTrendData(LocalDateTime start, LocalDateTime end) {
        List<Map<String, Object>> trend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        LocalDate current = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();

        while (!current.isAfter(endDate)) {
            LocalDateTime dayStart = current.atStartOfDay();
            LocalDateTime dayEnd = current.atTime(23, 59, 59);

            Long posterPush = pushRecordMapper.selectCount(
                    new LambdaQueryWrapper<PushRecord>()
                            .eq(PushRecord::getContentType, 1)
                            .ge(PushRecord::getPushTime, dayStart)
                            .le(PushRecord::getPushTime, dayEnd)
            );

            Long videoPush = pushRecordMapper.selectCount(
                    new LambdaQueryWrapper<PushRecord>()
                            .eq(PushRecord::getContentType, 2)
                            .ge(PushRecord::getPushTime, dayStart)
                            .le(PushRecord::getPushTime, dayEnd)
            );

            Long successPush = pushRecordMapper.selectCount(
                    new LambdaQueryWrapper<PushRecord>()
                            .eq(PushRecord::getPushStatus, 1)
                            .ge(PushRecord::getPushTime, dayStart)
                            .le(PushRecord::getPushTime, dayEnd)
            );

            Long totalPush = posterPush + videoPush;

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", current.format(formatter));
            dayData.put("total", totalPush.intValue());
            dayData.put("success", successPush.intValue());
            dayData.put("posterCount", posterPush.intValue());
            dayData.put("videoCount", videoPush.intValue());
            trend.add(dayData);

            current = current.plusDays(1);
        }

        return trend;
    }

    /**
     * 生成时段分布数据（按小时统计）
     */
    private int[] generateHourDistribution(LocalDateTime start, LocalDateTime end) {
        int[] hourDistribution = new int[24];

        List<PushRecord> records = pushRecordMapper.selectList(
                new LambdaQueryWrapper<PushRecord>()
                        .ge(PushRecord::getPushTime, start)
                        .le(PushRecord::getPushTime, end)
                        .select(PushRecord::getPushTime)
        );

        for (PushRecord record : records) {
            if (record.getPushTime() != null) {
                int hour = record.getPushTime().getHour();
                hourDistribution[hour]++;
            }
        }

        return hourDistribution;
    }

    /**
     * 生成设备推送排行
     */
    private List<Map<String, Object>> generateDeviceRank(LocalDateTime start, LocalDateTime end, int limit) {
        List<PushRecord> records = pushRecordMapper.selectList(
                new LambdaQueryWrapper<PushRecord>()
                        .ge(PushRecord::getPushTime, start)
                        .le(PushRecord::getPushTime, end)
                        .select(PushRecord::getTargetIds)
        );

        Map<Long, Integer> devicePushCount = new HashMap<>();

        for (PushRecord record : records) {
            if (record.getTargetIds() != null && !record.getTargetIds().isEmpty()) {
                try {
                    List<Long> deviceIds = JSONUtil.toList(record.getTargetIds(), Long.class);
                    for (Long id : deviceIds) {
                        devicePushCount.merge(id, 1, Integer::sum);
                    }
                } catch (Exception e) {
                    // 忽略解析错误
                }
            }
        }

        List<Map.Entry<Long, Integer>> sortedList = devicePushCount.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());

        if (sortedList.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> deviceIds = sortedList.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        List<Device> devices = deviceMapper.selectBatchIds(deviceIds);
        Map<Long, String> deviceNameMap = devices.stream()
                .collect(Collectors.toMap(Device::getId, Device::getDeviceName));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : sortedList) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", entry.getKey());
            item.put("name", deviceNameMap.getOrDefault(entry.getKey(), "未知设备"));
            item.put("count", entry.getValue());
            result.add(item);
        }

        return result;
    }

    /**
     * 统计设备的推送次数
     */
    private Long countDevicePushCount(Long deviceId, LocalDateTime start, LocalDateTime end) {
        List<PushRecord> records = pushRecordMapper.selectList(
                new LambdaQueryWrapper<PushRecord>()
                        .ge(PushRecord::getPushTime, start)
                        .le(PushRecord::getPushTime, end)
                        .select(PushRecord::getTargetIds)
        );

        long count = 0;
        for (PushRecord record : records) {
            if (record.getTargetIds() != null && !record.getTargetIds().isEmpty()) {
                try {
                    List<Long> deviceIds = JSONUtil.toList(record.getTargetIds(), Long.class);
                    if (deviceIds.contains(deviceId)) {
                        count++;
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        return count;
    }

    /**
     * 生成按日期的推送趋势
     */
    private List<Map<String, Object>> generateDailyPushTrend(LocalDateTime start, LocalDateTime end, Long filterDeviceId) {
        List<Map<String, Object>> trend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        LocalDate current = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();

        while (!current.isAfter(endDate)) {
            LocalDateTime dayStart = current.atStartOfDay();
            LocalDateTime dayEnd = current.atTime(23, 59, 59);

            Long dailyCount;
            if (filterDeviceId != null) {
                dailyCount = countDevicePushCount(filterDeviceId, dayStart, dayEnd);
            } else {
                dailyCount = pushRecordMapper.selectCount(
                        new LambdaQueryWrapper<PushRecord>()
                                .ge(PushRecord::getPushTime, dayStart)
                                .le(PushRecord::getPushTime, dayEnd)
                );
            }

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", current.format(formatter));
            dayData.put("count", dailyCount.intValue());
            trend.add(dayData);

            current = current.plusDays(1);
        }

        return trend;
    }

    /**
     * 获取推送状态文本
     */
    private String getPushStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "推送中";
            case 1: return "成功";
            case 2: return "失败";
            default: return "未知";
        }
    }

    /**
     * 格式化时长（秒转为可读格式）
     */
    private String formatDuration(long seconds) {
        if (seconds <= 0) return "0秒";

        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("天");
        if (hours > 0) sb.append(hours).append("小时");
        if (minutes > 0) sb.append(minutes).append("分钟");
        if (secs > 0 && days == 0) sb.append(secs).append("秒");

        return sb.length() > 0 ? sb.toString() : "0秒";
    }

    /**
     * 获取内容名称
     */
    private String getContentName(Integer contentType, Long contentId) {
        if (contentType == null || contentId == null) return "";

        if (contentType == 1) {
            // 海报
            Poster poster = posterMapper.selectById(contentId);
            return poster != null ? poster.getPosterName() : "";
        } else if (contentType == 2) {
            // 视频
            Video video = videoMapper.selectById(contentId);
            return video != null ? video.getVideoName() : "";
        }
        return "";
    }
}