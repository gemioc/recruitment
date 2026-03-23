package com.tv.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tv.recruitment.entity.Job;
import com.tv.recruitment.mapper.JobMapper;
import com.tv.recruitment.service.JobService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 职位服务实现
 */
@Service
public class JobServiceImpl extends ServiceImpl<JobMapper, Job> implements JobService {

    @Override
    public Page<Job> page(Integer pageNum, Integer pageSize, String jobName, String workAddress, Integer status) {
        LambdaQueryWrapper<Job> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(jobName)) {
            wrapper.like(Job::getJobName, jobName);
        }
        if (StringUtils.hasText(workAddress)) {
            wrapper.like(Job::getWorkAddress, workAddress);
        }
        if (status != null) {
            wrapper.eq(Job::getStatus, status);
        }

        wrapper.orderByDesc(Job::getCreateTime);

        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Job job = new Job();
        job.setId(id);
        job.setStatus(status);
        updateById(job);
    }

    @Override
    public void batchUpdateStatus(List<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        ids.forEach(id -> updateStatus(id, status));
    }
}