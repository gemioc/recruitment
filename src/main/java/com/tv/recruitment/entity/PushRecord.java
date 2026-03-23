package com.tv.recruitment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 推送记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_push_record")
public class PushRecord extends BaseEntity {

    /**
     * 内容类型: 1-海报 2-视频
     */
    private Integer contentType;

    /**
     * 内容ID
     */
    private Long contentId;

    /**
     * 内容名称
     */
    private String contentName;

    /**
     * 推送类型: 1-单台 2-多台 3-分组
     */
    private Integer pushType;

    /**
     * 推送目标ID列表（JSON数组）
     */
    private String targetIds;

    /**
     * 分组ID
     */
    private Long groupId;

    /**
     * 推送状态: 0-推送中 1-成功 2-失败
     */
    private Integer pushStatus;

    /**
     * 失败原因
     */
    private String failReason;

    /**
     * 播放规则（JSON）
     */
    private String playRule;

    /**
     * 推送人ID
     */
    private Long pushBy;

    /**
     * 推送时间
     */
    private LocalDateTime pushTime;

    /**
     * 完成时间
     */
    private LocalDateTime completeTime;
}