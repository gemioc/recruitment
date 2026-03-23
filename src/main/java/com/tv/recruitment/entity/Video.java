package com.tv.recruitment.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 视频实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_video")
public class Video extends BaseEntity {

    /**
     * 视频名称
     */
    private String videoName;

    /**
     * 视频文件路径
     */
    private String filePath;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 视频时长（秒）
     */
    private Integer duration;

    /**
     * 分辨率
     */
    private String resolution;

    /**
     * 是否置顶
     */
    private Integer isTop;

    /**
     * 上传人ID
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;
}