package com.tv.recruitment.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 设备状态枚举
 */
@Getter
@AllArgsConstructor
public enum DeviceStatusEnum {

    IN_USE(1, "在用"),
    IDLE(2, "闲置");

    private final Integer code;
    private final String desc;

    public static DeviceStatusEnum getByCode(Integer code) {
        for (DeviceStatusEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}