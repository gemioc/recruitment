package com.tv.recruitment.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 职位状态枚举
 */
@Getter
@AllArgsConstructor
public enum JobStatusEnum {

    RECRUITING(1, "招聘中"),
    EXPIRED(2, "已截止");

    private final Integer code;
    private final String desc;

    public static JobStatusEnum getByCode(Integer code) {
        for (JobStatusEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}