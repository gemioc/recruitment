package com.tv.recruitment.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 通用错误
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 用户相关 1000-1099
    USER_NOT_FOUND(1001, "用户不存在"),
    PASSWORD_ERROR(1002, "用户名或密码错误"),
    USER_DISABLED(1003, "账号已被禁用"),
    USERNAME_EXISTS(1004, "用户名已存在"),
    TOKEN_EXPIRED(1005, "Token已过期"),
    TOKEN_INVALID(1006, "Token无效"),

    // 设备相关 2000-2099
    DEVICE_NOT_FOUND(2001, "设备不存在"),
    DEVICE_OFFLINE(2002, "设备离线"),
    DEVICE_CODE_EXISTS(2003, "设备编码已存在"),
    DEVICE_GROUP_HAS_DEVICES(2004, "分组下存在设备，无法删除"),

    // 职位相关 3000-3099
    JOB_NOT_FOUND(3001, "职位不存在"),
    JOB_EXPIRED(3002, "职位已截止"),

    // 海报相关 4000-4099
    POSTER_NOT_FOUND(4001, "海报不存在"),
    POSTER_GENERATE_ERROR(4002, "海报生成失败"),
    TEMPLATE_NOT_FOUND(4003, "模板不存在"),

    // 视频相关 5000-5099
    VIDEO_NOT_FOUND(5001, "视频不存在"),
    FILE_UPLOAD_ERROR(5002, "文件上传失败"),
    FILE_TYPE_ERROR(5003, "文件类型不支持"),
    FILE_SIZE_EXCEED(5004, "文件大小超过限制");

    private final Integer code;
    private final String message;
}