package com.tv.recruitment.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 职位实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_job")
public class Job extends BaseEntity {

    /**
     * 职位名称
     */
    private String jobName;

    /**
     * 薪资下限
     */
    private BigDecimal salaryMin;

    /**
     * 薪资上限
     */
    private BigDecimal salaryMax;

    /**
     * 工作地址
     */
    private String workAddress;

    /**
     * 任职要求
     */
    private String requirements;

    /**
     * 招聘人数
     */
    private Integer recruitCount;

    /**
     * 岗位职责
     */
    private String responsibilities;

    /**
     * 公司福利
     */
    private String welfare;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 联系微信
     */
    private String contactWechat;

    /**
     * 截止日期
     */
    private LocalDate deadline;

    /**
     * 状态: 1-招聘中 2-已截止
     */
    private Integer status;

    /**
     * 创建人ID
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 更新人ID
     */
    @TableField(fill = FieldFill.UPDATE)
    private Long updateBy;
}