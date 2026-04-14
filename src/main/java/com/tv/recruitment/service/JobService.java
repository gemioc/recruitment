package com.tv.recruitment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tv.recruitment.entity.Job;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 职位服务
 *
 * @author tv_recru
 */
public interface JobService extends IService<Job> {

    /**
     * 分页查询职位
     *
     * @param pageNum      页码
     * @param pageSize     每页大小
     * @param jobName      职位名称（模糊查询）
     * @param workAddress  工作地点（模糊查询）
     * @param status       状态
     * @return 分页结果
     */
    Page<Job> page(Integer pageNum, Integer pageSize, String jobName, String workAddress, Integer status);

    /**
     * 更新状态
     *
     * @param id     职位ID
     * @param status 状态
     */
    void updateStatus(Long id, Integer status);

    /**
     * 批量更新状态
     *
     * @param ids    职位ID列表
     * @param status 状态
     */
    void batchUpdateStatus(List<Long> ids, Integer status);

    /**
     * 批量删除职位
     *
     * @param ids 职位ID列表
     */
    void batchDelete(List<Long> ids);

    /**
     * 下载职位导入模板
     *
     * @param response 响应对象
     * @throws IOException IO异常
     */
    void downloadTemplate(HttpServletResponse response) throws IOException;

    /**
     * 批量导入职位
     *
     * @param file Excel文件
     * @return 导入结果 {total, success, fail, errors}
     * @throws IOException IO异常
     */
    Map<String, Object> importJobs(MultipartFile file) throws IOException;

    /**
     * 获取职位统计数据
     *
     * @return 统计数据 {total, recruitingCount, expiredCount, todayCount}
     */
    Map<String, Object> getStats();
}
