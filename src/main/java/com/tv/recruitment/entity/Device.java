package com.tv.recruitment.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 设备实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_device")
public class Device extends BaseEntity {

    /**
     * 设备编码
     */
    private String deviceCode;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备位置
     */
    private String location;

    /**
     * 分组ID
     */
    private Long groupId;

    /**
     * 分辨率
     */
    private String resolution;

    /**
     * 备注
     */
    private String remark;

    /**
     * 使用状态: 1-在用 2-闲置
     */
    private Integer status;

    /**
     * 在线状态: 0-离线 1-在线
     */
    private Integer onlineStatus;

    /**
     * 最后心跳时间
     */
    private LocalDateTime lastHeartbeat;

    /**
     * 当前展示内容类型: 1-海报 2-视频
     */
    private Integer currentContentType;

    /**
     * 当前展示内容ID
     */
    private Long currentContentId;

    /**
     * 播放状态: 1-播放中 2-暂停
     */
    private Integer playStatus;

    /**
     * 分组名称（非数据库字段）
     */
    @TableField(exist = false)
    private String groupName;
}