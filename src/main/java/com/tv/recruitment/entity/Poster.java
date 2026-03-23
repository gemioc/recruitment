package com.tv.recruitment.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 海报实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_poster")
public class Poster extends BaseEntity {

    /**
     * 海报名称
     */
    private String posterName;

    /**
     * 关联职位ID
     */
    private Long jobId;

    /**
     * 使用的模板ID
     */
    private Long templateId;

    /**
     * 海报文件路径
     */
    private String filePath;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 创建人ID
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 更新时间（表中不存在此字段）
     */
    @TableField(exist = false)
    private LocalDateTime updateTime;
}