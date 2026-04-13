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

    @ExcelProperty("职位名称*")
    private String jobName;

    @ExcelProperty("公司名称*")
    private String company;

    @ExcelProperty("薪资下限*")
    private Integer salaryMin;

    @ExcelProperty("薪资上限*")
    private Integer salaryMax;

    @ExcelProperty("工作地点*")
    private String workAddress;

    @ExcelProperty("学历要求")
    private String education;

    @ExcelProperty("工作经验")
    private String experience;

    @ExcelProperty("招聘人数")
    private Integer recruitCount;

    @ExcelProperty("联系人")
    private String contactName;

    @ExcelProperty("联系电话*")
    private String contactPhone;

    @ExcelProperty("联系邮箱")
    private String contactEmail;

    @ExcelProperty("职位信息")
    private String jobInfo;

    @ExcelProperty("福利待遇")
    private String welfare;

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