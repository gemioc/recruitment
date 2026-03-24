package com.tv.recruitment.common.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    /**
     * 操作类型
     */
    String type();

    /**
     * 操作描述
     */
    String desc() default "";

    /**
     * 是否保存请求参数
     */
    boolean saveRequest() default true;

    /**
     * 是否保存响应结果
     */
    boolean saveResponse() default false;

    /**
     * 排除的请求参数字段（敏感字段）
     */
    String[] excludeParams() default {"password", "oldPassword", "newPassword", "token"};
}