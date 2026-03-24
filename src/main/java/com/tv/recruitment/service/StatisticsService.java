package com.tv.recruitment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 统计服务接口
 */
public interface StatisticsService {

    /**
     * 获取推送统计概览
     */
    Map<String, Object> getPushStatistics(String startDate, String endDate, String type);

    /**
     * 获取推送记录明细列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param deviceId 设备ID
     * @param contentType 内容类型：1-海报 2-视频
     * @param pushStatus 推送状态：0-推送中 1-成功 2-失败
     */
    Page<Map<String, Object>> getPushRecordList(Integer pageNum, Integer pageSize,
            String startDate, String endDate, Long deviceId, Integer contentType, Integer pushStatus);

    /**
     * 获取设备状态统计
     * @param deviceId 设备ID（可选，不传则统计所有设备）
     * @param startDate 开始日期
     * @param endDate 结束日期
     */
    Map<String, Object> getDeviceStatusStatistics(Long deviceId, String startDate, String endDate);

    /**
     * 获取设备统计
     */
    Map<String, Object> getDeviceStatistics();

    /**
     * 获取内容统计
     */
    Map<String, Object> getContentStatistics();

    /**
     * 导出推送记录Excel
     */
    void exportPushRecords(String startDate, String endDate, Long deviceId,
            Integer contentType, Integer pushStatus, HttpServletResponse response);

    /**
     * 导出统计数据
     */
    void exportStatistics(String startDate, String endDate, HttpServletResponse response);
}