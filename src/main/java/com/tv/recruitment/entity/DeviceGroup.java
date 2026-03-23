package com.tv.recruitment.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 设备分组实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_device_group")
public class DeviceGroup extends BaseEntity {

    /**
     * 分组名称
     */
    private String groupName;

    /**
     * 分组描述
     */
    private String description;

    /**
     * 创建人ID
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;
}