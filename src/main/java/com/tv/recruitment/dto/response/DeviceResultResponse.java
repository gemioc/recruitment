package com.tv.recruitment.dto.response;

import lombok.Data;

/**
 * 设备推送结果
 */
@Data
public class DeviceResultResponse {
    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 推送状态：1-成功 0-失败
     */
    private Integer status;

    /**
     * 结果信息
     */
    private String message;
}