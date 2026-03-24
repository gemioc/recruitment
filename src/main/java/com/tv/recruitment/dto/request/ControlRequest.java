package com.tv.recruitment.dto.request;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 设备控制请求
 */
@Data
public class ControlRequest {
    /**
     * 设备ID列表
     */
    private List<Long> deviceIds;

    /**
     * 控制动作：restart/shutdown/volume等
     */
    private String action;

    /**
     * 控制参数（如音量值等）
     */
    private Map<String, Object> params;
}