package com.tv.recruitment.service;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 统计服务接口
 */
public interface StatisticsService {

    /**
     * 获取推送统计
     */
    Map<String, Object> getPushStatistics(String startDate, String endDate, String type);

    /**
     * 获取设备统计
     */
    Map<String, Object> getDeviceStatistics();

    /**
     * 获取内容统计
     */
    Map<String, Object> getContentStatistics();

    /**
     * 导出统计数据
     */
    void exportStatistics(String startDate, String endDate, HttpServletResponse response);
}