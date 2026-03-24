package com.tv.recruitment.dto.response;

import lombok.Data;

/**
 * 用户信息响应
 */
@Data
public class UserInfoResponse {
    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 角色ID
     */
    private Integer role;

    /**
     * 角色名称
     */
    private String roleName;
}