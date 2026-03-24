package com.tv.recruitment.dto.request;

import lombok.Data;

import java.util.List;

/**
 * 推送请求
 */
@Data
public class PushRequest {
    /**
     * 海报ID（推送海报时使用）
     */
    private Long posterId;

    /**
     * 视频ID（推送视频时使用）
     */
    private Long videoId;

    /**
     * 内容类型：poster/video
     */
    private String contentType;

    /**
     * 内容ID列表（批量推送时使用）
     */
    private List<Long> contentIds;

    /**
     * 单个内容ID
     */
    private Long contentId;

    /**
     * 推送类型
     */
    private Integer pushType;

    /**
     * 目标设备ID列表
     */
    private List<Long> targetIds;

    /**
     * 分组ID
     */
    private Long groupId;

    /**
     * 播放规则
     */
    private Object playRule;
}