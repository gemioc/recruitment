package com.tv.recruitment.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tv.recruitment.entity.Job;
import com.tv.recruitment.listener.JobImportListener;
import com.tv.recruitment.mapper.JobMapper;
import com.tv.recruitment.service.JobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 职位服务实现
 *
 * @author tv_recru
 */
@Slf4j
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

    @Override
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        removeByIds(ids);
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        // 读取Excel模板文件
        ClassPathResource resource = new ClassPathResource("templates/job_import_template.xlsx");

        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = URLEncoder.encode("职位导入模板", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

        // 写入文件内容
        try (InputStream inputStream = resource.getInputStream();
             OutputStream outputStream = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    @Override
    public Map<String, Object> importJobs(MultipartFile file) throws IOException {
        // 创建监听器
        JobImportListener listener = new JobImportListener();

        // 读取Excel，默认读取第一个sheet，headRowNumber为1（第一行是表头）
        EasyExcel.read(file.getInputStream(), Job.class, listener)
                .sheet(0)
                .headRowNumber(1)
                .doRead();

        List<Job> successData = listener.getSuccessData();
        List<String> errors = listener.getErrors();

        log.info("解析到有效数据{}条，错误{}条", successData.size(), errors.size());

        // 保存职位
        int successCount = 0;
        for (Job job : successData) {
            try {
                // 设置默认值
                if (job.getEducation() == null) {
                    job.setEducation("不限");
                }
                if (job.getExperience() == null) {
                    job.setExperience("不限");
                }
                if (job.getRecruitCount() == null) {
                    job.setRecruitCount(1);
                }
                job.setStatus(1);
                log.info("保存职位: {}", job);
                save(job);
                successCount++;
            } catch (Exception e) {
                log.error("职位[{}]保存失败: {}", job.getJobName(), e.getMessage(), e);
                errors.add("职位「" + job.getJobName() + "」保存失败：" + e.getMessage());
            }
        }

        log.info("实际保存成功{}条", successCount);

        Map<String, Object> result = new HashMap<>();
        result.put("total", successCount + errors.size());
        result.put("success", successCount);
        result.put("fail", errors.size());
        result.put("errors", errors);

        return result;
    }

    @Override
    public Map<String, Object> getStats() {
        long total = baseMapper.selectCount(null);

        LambdaQueryWrapper<Job> recruitingWrapper = new LambdaQueryWrapper<>();
        recruitingWrapper.eq(Job::getStatus, 1);
        long recruitingCount = baseMapper.selectCount(recruitingWrapper);

        long expiredCount = total - recruitingCount;

        LambdaQueryWrapper<Job> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(Job::getCreateTime, LocalDate.now().atStartOfDay());
        todayWrapper.lt(Job::getCreateTime, LocalDate.now().plusDays(1).atStartOfDay());
        long todayCount = baseMapper.selectCount(todayWrapper);

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("recruitingCount", recruitingCount);
        stats.put("expiredCount", expiredCount);
        stats.put("todayCount", todayCount);
        return stats;
    }
}
