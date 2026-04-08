package com.tv.recruitment.controller;

import com.tv.recruitment.common.annotation.Log;
import com.tv.recruitment.common.result.Result;
import com.tv.recruitment.dto.request.LoginRequest;
import com.tv.recruitment.dto.response.LoginResponse;
import com.tv.recruitment.dto.response.UserInfoResponse;
import com.tv.recruitment.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 *
 * @author tv_recru
 */
@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    @Log(type = "LOGIN", desc = "用户登录", saveRequest = false, saveResponse = false)
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/userInfo")
    public Result<UserInfoResponse> getUserInfo() {
        return Result.success(authService.getCurrentUser());
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    @Log(type = "UPDATE_PASSWORD", desc = "修改密码", saveRequest = false)
    public Result<Void> updatePassword(@RequestBody Map<String, String> params) {
        authService.updatePassword(params.get("oldPassword"), params.get("newPassword"));
        return Result.success();
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    @Log(type = "LOGOUT", desc = "用户退出")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }

    @Operation(summary = "生成密码哈希(测试用)")
    @GetMapping("/encodePassword")
    public Result<Map<String, Object>> encodePassword(@RequestParam String password) {
        return Result.success(authService.encodePassword(password));
    }

    @Operation(summary = "重置admin密码")
    @PostMapping("/resetAdmin")
    public Result<Map<String, Object>> resetAdmin() {
        return Result.success(authService.resetAdminPassword());
    }
}