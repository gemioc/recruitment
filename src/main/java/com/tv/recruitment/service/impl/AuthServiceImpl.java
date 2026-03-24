package com.tv.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tv.recruitment.common.exception.BusinessException;
import com.tv.recruitment.common.exception.ErrorCode;
import com.tv.recruitment.common.utils.JwtUtils;
import com.tv.recruitment.common.utils.SecurityUtils;
import com.tv.recruitment.dto.request.LoginRequest;
import com.tv.recruitment.dto.response.LoginResponse;
import com.tv.recruitment.dto.response.UserInfoResponse;
import com.tv.recruitment.entity.User;
import com.tv.recruitment.mapper.UserMapper;
import com.tv.recruitment.security.UserPrincipal;
import com.tv.recruitment.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Base64;

/**
 * 认证服务实现
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public LoginResponse login(LoginRequest request) {
        // 查询用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );

        if (user == null) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        // Base64解码密码
        String password = new String(Base64.getDecoder().decode(request.getPassword()));

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        // 检查状态
        if (user.getStatus() == 2) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }

        // 更新最后登录时间
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(updateUser);

        // 生成Token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());

        // 构建响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setExpiresIn(86400L);

        UserInfoResponse userInfo = new UserInfoResponse();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setRole(user.getRole());
        userInfo.setRoleName(user.getRole() == 1 ? "管理员" : "运营人员");
        response.setUserInfo(userInfo);

        return response;
    }

    @Override
    public UserInfoResponse getCurrentUser() {
        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal();
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        User user = userMapper.selectById(principal.getUserId());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        UserInfoResponse userInfo = new UserInfoResponse();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setRole(user.getRole());
        userInfo.setRoleName(user.getRole() == 1 ? "管理员" : "运营人员");

        return userInfo;
    }

    @Override
    public void updatePassword(String oldPassword, String newPassword) {
        UserPrincipal principal = SecurityUtils.getCurrentUserPrincipal();
        if (principal == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        User user = userMapper.selectById(principal.getUserId());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // Base64解码密码
        String decodedOldPassword = new String(Base64.getDecoder().decode(oldPassword));
        String decodedNewPassword = new String(Base64.getDecoder().decode(newPassword));

        // 验证旧密码
        if (!passwordEncoder.matches(decodedOldPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        // 更新密码
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setPassword(passwordEncoder.encode(decodedNewPassword));
        userMapper.updateById(updateUser);
    }

    @Override
    public void logout() {
        // Token无状态，无需特殊处理
    }

    @Override
    public void updateAdminPassword(String encodedPassword) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, "admin")
        );
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setPassword(encodedPassword);
        userMapper.updateById(updateUser);
    }
}