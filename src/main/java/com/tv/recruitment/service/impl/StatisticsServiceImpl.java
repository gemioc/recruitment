package com.tv.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tv.recruitment.common.utils.CsvExportUtils;
import com.tv.recruitment.entity.*;
import com.tv.recruitment.mapper.*;
import com.tv.recruitment.service.StatisticsService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
        List<Map<String, Object>> trend = generateTrendData(start, end, type);
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

        // 时段分布（简化处理）
        int[] hourDistribution = new int[24];
        // 实际应该按小时分组查询，这里简化处理
        result.put("hourDistribution", hourDistribution);

        // 设备推送排行
        result.put("deviceRank", new ArrayList<>());

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

        // 职位统计
        Long jobCount = jobMapper.selectCount(
                new LambdaQueryWrapper<Job>().eq(Job::getStatus, 1)
        );
        result.put("jobCount", jobCount);

        // 今日新增职位
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
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

        // 内容类型分布
        List<Map<String, Object>> typeDistribution = new ArrayList<>();

        Map<String, Object> jobMap = new HashMap<>();
        jobMap.put("type", "job");
        jobMap.put("count", jobCount);
        typeDistribution.add(jobMap);

        Map<String, Object> posterMap = new HashMap<>();
        posterMap.put("type", "poster");
        posterMap.put("count", posterCount);
        typeDistribution.add(posterMap);

        Map<String, Object> videoMap = new HashMap<>();
        videoMap.put("type", "video");
        videoMap.put("count", videoCount);
        typeDistribution.add(videoMap);

        result.put("typeDistribution", typeDistribution);

        return result;
    }

    @Override
    public void exportStatistics(String startDate, String endDate, HttpServletResponse response) {
        try {
            // 设置CSV响应头
            String fileName = "统计数据_" + LocalDate.now();
            CsvExportUtils.setCsvResponseHeaders(response, fileName);

            // 导出CSV格式
            OutputStream out = response.getOutputStream();
            CsvExportUtils.writeBom(out);

            StringBuilder sb = new StringBuilder();
            sb.append("类型,数量\n");

            Map<String, Object> deviceStats = getDeviceStatistics();
            sb.append(CsvExportUtils.buildCsvLine("设备总数", String.valueOf(deviceStats.get("total"))));
            sb.append(CsvExportUtils.buildCsvLine("在线设备", String.valueOf(deviceStats.get("online"))));
            sb.append(CsvExportUtils.buildCsvLine("离线设备", String.valueOf(deviceStats.get("offline"))));

            Map<String, Object> contentStats = getContentStatistics();
            sb.append(CsvExportUtils.buildCsvLine("职位数量", String.valueOf(contentStats.get("jobCount"))));
            sb.append(CsvExportUtils.buildCsvLine("海报数量", String.valueOf(contentStats.get("posterCount"))));
            sb.append(CsvExportUtils.buildCsvLine("视频数量", String.valueOf(contentStats.get("videoCount"))));

            out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.close();
        } catch (IOException e) {
            log.error("导出统计数据失败", e);
        }
    }

    /**
     * 计算起始时间
     */
    private LocalDateTime calculateStartDate(String type) {
        LocalDateTime now = LocalDateTime.now();
        switch (type) {
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
    private List<Map<String, Object>> generateTrendData(LocalDateTime start, LocalDateTime end, String type) {
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

            Long totalPush = posterPush + videoPush;

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", current.format(formatter));
            dayData.put("total", totalPush);
            dayData.put("success", totalPush); // 简化处理
            dayData.put("posterCount", posterPush);
            dayData.put("videoCount", videoPush);
            trend.add(dayData);

            current = current.plusDays(1);
        }

        return trend;
    }
}