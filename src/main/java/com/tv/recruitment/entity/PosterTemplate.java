package com.tv.recruitment.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 海报模板实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_poster_template")
public class PosterTemplate extends BaseEntity {

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 模板标识（对应前端 posterTemplateEngine.js 中的模板注册ID）
     */
    @TableField("template_id")
    private String templateId;

    /**
     * 配色方案
     */
    private String colorScheme;

    /**
     * 预览图路径
     */
    private String previewPath;

    /**
     * 是否默认模板
     */
    private Integer isDefault;

    /**
     * 状态: 1-启用 2-禁用
     */
    private Integer status;

    /**
     * 创建人ID
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;
}