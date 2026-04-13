package com.tv.recruitment.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 图片实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_image")
public class Image extends BaseEntity {

    /**
     * 图片名称
     */
    private String imageName;

    /**
     * 图片文件路径
     */
    private String filePath;

    /**
     * 缩略图路径
     */
    private String thumbnailPath;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 图片宽度
     */
    private Integer width;

    /**
     * 图片高度
     */
    private Integer height;

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