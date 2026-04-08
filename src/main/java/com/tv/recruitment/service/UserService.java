package com.tv.recruitment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tv.recruitment.entity.User;

import java.util.List;

/**
 * 用户服务
 *
 * @author tv_recru
 */
public interface UserService extends IService<User> {

    /**
     * 分页查询用户
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param username 用户名（模糊查询）
     * @param role     角色
     * @param status   状态
     * @return 分页结果
     */
    Page<User> page(Integer pageNum, Integer pageSize, String username, Integer role, Integer status);

    /**
     * 新增用户
     *
     * @param user 用户信息
     */
    void saveUser(User user);

    /**
     * 编辑用户
     *
     * @param id   用户ID
     * @param user 用户信息
     */
    void updateUser(Long id, User user);

    /**
     * 更新用户状态
     *
     * @param id     用户ID
     * @param status 状态
     */
    void updateStatus(Long id, Integer status);

    /**
     * 重置密码
     *
     * @param id          用户ID
     * @param newPassword 新密码
     */
    void resetPassword(Long id, String newPassword);

    /**
     * 批量更新用户状态
     *
     * @param ids    用户ID列表
     * @param status 状态
     */
    void batchUpdateStatus(List<Long> ids, Integer status);
}
