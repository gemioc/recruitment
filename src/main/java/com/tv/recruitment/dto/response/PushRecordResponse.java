package com.tv.recruitment.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 推送记录响应
 */
@Data
public class PushRecordResponse {
    /**
     * 推送记录ID
     */
    private Long id;

    /**
     * 内容类型：poster/video
     */
    private String contentType;

    /**
     * 内容名称
     */
    private String contentTitle;

    /**
     * 推送类型：1-单台 2-多台 3-分组
     */
    private Integer pushType;

    /**
     * 分组名称（分组推送时）
     */
    private String groupName;

    /**
     * 目标设备名称
     */
    private String deviceNames;

    /**
     * 设备数量
     */
    private Integer deviceCount;

    /**
     * 成功数量
     */
    private Integer successCount;

    /**
     * 失败数量
     */
    private Integer failCount;

    /**
     * 推送状态：0-推送中 1-成功 2-失败
     */
    private Integer status;

    /**
     * 操作人名称
     */
    private String operatorName;

    /**
     * 推送时间
     */
    private LocalDateTime createTime;

    /**
     * 设备推送结果（详情时使用）
     */
    private List<DeviceResultResponse> deviceResults;
}