package com.tv.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tv.recruitment.entity.User;
import com.tv.recruitment.mapper.UserMapper;
import com.tv.recruitment.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户服务实现
 *
 * @author tv_recru
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<User> page(Integer pageNum, Integer pageSize, String username, Integer role, Integer status) {
        Page<User> page = new Page<>(pageNum, pageSize);
        Page<User> result = userMapper.selectPage(page,
                new LambdaQueryWrapper<User>()
                        .like(username != null, User::getUsername, username)
                        .eq(role != null, User::getRole, role)
                        .eq(status != null, User::getStatus, status)
                        .orderByDesc(User::getCreateTime));

        // 清除密码
        result.getRecords().forEach(u -> u.setPassword(null));
        return result;
    }

    @Override
    public void saveUser(User user) {
        user.setPassword(passwordEncoder.encode("123456"));
        userMapper.insert(user);
    }

    @Override
    public void updateUser(Long id, User user) {
        user.setId(id);
        user.setPassword(null);
        userMapper.updateById(user);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    public void resetPassword(Long id, String newPassword) {
        User user = new User();
        user.setId(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    @Override
    public void batchUpdateStatus(List<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        ids.forEach(id -> {
            User user = new User();
            user.setId(id);
            user.setStatus(status);
            userMapper.updateById(user);
        });
    }
}
