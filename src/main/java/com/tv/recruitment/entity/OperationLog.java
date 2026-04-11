package com.tv.recruitment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志实体
 */
@Data
@TableName("t_operation_log")
public class OperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 操作用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 操作类型
     */
    private String operationType;

    /**
     * 操作描述
     */
    private String operationDesc;

    /**
     * 请求方法
     */
    private String requestMethod;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 响应结果
     */
    private String responseResult;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 操作时间
     */
    private LocalDateTime operationTime;

    /**
     * 执行时长（毫秒）
     */
    private Long executionTime;
}