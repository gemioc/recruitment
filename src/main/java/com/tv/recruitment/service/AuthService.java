package com.tv.recruitment.service;

import com.tv.recruitment.dto.request.LoginRequest;
import com.tv.recruitment.dto.response.LoginResponse;
import com.tv.recruitment.dto.response.UserInfoResponse;

import java.util.Map;

/**
 * 认证服务
 *
 * @author tv_recru
 */
public interface AuthService {

    /**
     * 登录
     *
     * @param request 登录请求
     * @return 登录响应
     */
    LoginResponse login(LoginRequest request);

    /**
     * 获取当前用户信息
     *
     * @return 当前用户信息
     */
    UserInfoResponse getCurrentUser();

    /**
     * 修改密码
     *
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void updatePassword(String oldPassword, String newPassword);

    /**
     * 退出登录
     */
    void logout();

    /**
     * 更新admin密码
     *
     * @param encodedPassword 加密后的密码
     */
    void updateAdminPassword(String encodedPassword);

    /**
     * 生成密码哈希(测试用)
     *
     * @param password 明文密码
     * @return 包含明文密码和哈希密码的Map
     */
    Map<String, Object> encodePassword(String password);

    /**
     * 重置admin密码
     *
     * @return 包含原始密码和哈希密码的Map
     */
    Map<String, Object> resetAdminPassword();
}