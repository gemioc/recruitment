package com.tv.recruitment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tv.recruitment.dto.request.ControlRequest;
import com.tv.recruitment.dto.request.PushRequest;
import com.tv.recruitment.dto.response.PushRecordResponse;
import com.tv.recruitment.entity.PushRecord;

import java.util.List;
import java.util.Map;

/**
 * 推送服务接口
 */
public interface PushService {

    /**
     * 推送海报
     */
    Long pushPoster(PushRequest request);

    /**
     * 推送视频
     */
    Long pushVideo(PushRequest request);

    /**
     * 设备控制
     */
    void control(ControlRequest request);

    /**
     * 获取推送记录
     */
    Page<PushRecordResponse> getRecords(Integer pageNum, Integer pageSize, Integer contentType,
            Integer pushStatus, String startDate, String endDate, String deviceName);

    /**
     * 获取推送记录详情
     */
    PushRecordResponse getRecordDetail(Long id);

    /**
     * 批量推送
     */
    Long pushMultiple(PushRequest request);

    /**
     * 获取分组下的设备ID列表
     */
    List<Long> getDeviceIdsByGroup(Long groupId);

    /**
     * 获取分组信息（含设备统计）
     */
    List<Map<String, Object>> getPushGroups();

    /**
     * 解析目标设备ID列表
     * 支持按分组推送和按设备ID推送
     */
    List<Long> resolveTargetIds(PushRequest request);

    /**
     * 确定推送类型
     * 1-单台推送 2-多台推送 3-分组推送
     */
    Integer determinePushType(PushRequest request);

    /**
     * 执行推送
     */
    void executePush(PushRecord record, List<Long> targetIds);

    /**
     * 执行多内容推送（支持轮播）
     */
    void executePushMultiple(PushRecord record, List<Long> targetIds, List<Long> contentIds, int contentTypeInt);

    /**
     * 转换为响应对象
     */
    PushRecordResponse convertToResponse(PushRecord record);

    /**
     * 构建查询条件
     */
    LambdaQueryWrapper<PushRecord> buildQueryWrapper(Integer contentType, Integer pushStatus,
            String startDate, String endDate, String deviceName);

    /**
     * 获取推送类型文本
     */
    String getPushTypeText(Integer pushType);

    /**
     * 获取状态文本
     */
    String getStatusText(Integer status);

    /**
     * 获取所有推送记录（用于导出）
     */
    List<PushRecord> getAllRecords(Integer contentType, Integer pushStatus, String startDate,
            String endDate, String deviceName);
}
