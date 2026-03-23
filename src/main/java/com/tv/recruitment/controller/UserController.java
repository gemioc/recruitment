package com.tv.recruitment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tv.recruitment.common.result.Result;
import com.tv.recruitment.entity.User;
import com.tv.recruitment.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "分页查询用户")
    @GetMapping
    public Result<Page<User>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer role,
            @RequestParam(required = false) Integer status) {

        Page<User> page = new Page<>(pageNum, pageSize);
        Page<User> result = userMapper.selectPage(page,
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .like(username != null, User::getUsername, username)
                        .eq(role != null, User::getRole, role)
                        .eq(status != null, User::getStatus, status)
                        .orderByDesc(User::getCreateTime));

        // 清除密码
        result.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(result);
    }

    @Operation(summary = "新增用户")
    @PostMapping
    public Result<Void> save(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode("123456"));
        userMapper.insert(user);
        return Result.success();
    }

    @Operation(summary = "编辑用户")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        user.setPassword(null);
        userMapper.updateById(user);
        return Result.success();
    }

    @Operation(summary = "启用/禁用用户")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        userMapper.updateById(user);
        return Result.success();
    }

    @Operation(summary = "重置密码")
    @PutMapping("/{id}/resetPassword")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody String newPassword) {
        User user = new User();
        user.setId(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        return Result.success();
    }

    @Operation(summary = "批量启用/禁用")
    @PutMapping("/batch/status")
    public Result<Void> batchUpdateStatus(@RequestBody List<Long> ids, @RequestParam Integer status) {
        ids.forEach(id -> {
            User user = new User();
            user.setId(id);
            user.setStatus(status);
            userMapper.updateById(user);
        });
        return Result.success();
    }
}