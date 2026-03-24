package com.tv.recruitment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 操作类型响应
 */
@Data
@AllArgsConstructor
public class OperationTypeResponse {
    /**
     * 操作类型代码
     */
    private String code;

    /**
     * 操作类型名称
     */
    private String name;
}