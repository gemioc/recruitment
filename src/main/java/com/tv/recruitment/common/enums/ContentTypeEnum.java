package com.tv.recruitment.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 内容类型枚举
 */
@Getter
@AllArgsConstructor
public enum ContentTypeEnum {

    POSTER(1, "海报"),
    VIDEO(2, "视频"),
    IMAGE(3, "图片");

    private final Integer code;
    private final String desc;

    public static ContentTypeEnum getByCode(Integer code) {
        for (ContentTypeEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}