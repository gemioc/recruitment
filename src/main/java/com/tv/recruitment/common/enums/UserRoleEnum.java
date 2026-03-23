package com.tv.recruitment.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
@AllArgsConstructor
public enum UserRoleEnum {

    ADMIN(1, "管理员"),
    OPERATOR(2, "运营人员");

    private final Integer code;
    private final String desc;

    public static UserRoleEnum getByCode(Integer code) {
        for (UserRoleEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}