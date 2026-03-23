package com.tv.recruitment.service;

import com.tv.recruitment.dto.request.LoginRequest;
import com.tv.recruitment.dto.response.LoginResponse;

/**
 * 认证服务
 */
public interface AuthService {

    /**
     * 登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 获取当前用户信息
     */
    LoginResponse.UserInfo getCurrentUser();

    /**
     * 修改密码
     */
    void updatePassword(String oldPassword, String newPassword);

    /**
     * 退出登录
     */
    void logout();
}