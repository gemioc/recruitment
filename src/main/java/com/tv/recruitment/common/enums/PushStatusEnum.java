package com.tv.recruitment.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 推送状态枚举
 */
@Getter
@AllArgsConstructor
public enum PushStatusEnum {

    PUSHING(0, "推送中"),
    SUCCESS(1, "推送成功"),
    FAILED(2, "推送失败");

    private final Integer code;
    private final String desc;

    public static PushStatusEnum getByCode(Integer code) {
        for (PushStatusEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}