package com.tv.recruitment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tv.recruitment.entity.Job;

/**
 * 职位服务
 */
public interface JobService extends IService<Job> {

    /**
     * 分页查询职位
     */
    Page<Job> page(Integer pageNum, Integer pageSize, String jobName, String workAddress, Integer status);

    /**
     * 更新状态
     */
    void updateStatus(Long id, Integer status);

    /**
     * 批量更新状态
     */
    void batchUpdateStatus(java.util.List<Long> ids, Integer status);
}