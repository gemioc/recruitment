package com.tv.recruitment.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.FieldFill;
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
     * 展位号码
     */
    @ExcelProperty("展位号码")
    private String number;

    /**
     * 公司名称
     */
    @ExcelProperty("单位名称*")
    private String company;

    /**
     * 工作地
     */
    @ExcelProperty("职位行政区划")
    private String workAddress;

    /**
     * 职位名称
     */
    @ExcelProperty("工种名称*")
    private String jobName;

    /**
     * 工种类别
     */
    @ExcelProperty("工种类别*")
    private String jobType;

    /**
     * 招聘人数
     */
    @ExcelProperty("招聘人数")
    private Integer recruitCount;

    /**
     * 薪资下限
     */
    @ExcelProperty("月薪下限*")
    private Integer salaryMin;

    /**
     * 薪资上限
     */
    @ExcelProperty("月薪上限*")
    private Integer salaryMax;

    /**
     * 福利待遇
     */
    @ExcelProperty("福利待遇")
    private String welfare;

    /**
     * 职位信息
     */
    @ExcelProperty("职位简介")
    private String jobInfo;

    /**
     * 工作性质
     */
    @ExcelProperty("工作性质")
    private String workNature;

    /**
     * 学历要求
     */
    @ExcelProperty("学历要求")
    private String education;

    /**
     * 学历要求
     */
    @ExcelProperty("经验要求")
    private String experience;

    /**
     * 联系人
     */
    @ExcelProperty("联系人")
    private String contactName;

    /**
     * 联系电话
     */
    @ExcelProperty("联系手机*")
    private String contactPhone;

    /**
     * 联系邮箱
     */
    @ExcelProperty("电子邮箱")
    private String contactEmail;

    @ExcelIgnore
    private String contactWechat;

    @ExcelIgnore
    private LocalDate deadline;

    @ExcelIgnore
    private Integer status;

    @ExcelIgnore
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @ExcelIgnore
    @TableField(fill = FieldFill.UPDATE)
    private Long updateBy;
}