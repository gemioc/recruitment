package com.tv.recruitment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tv.recruitment.dto.response.OperationTypeResponse;
import com.tv.recruitment.entity.OperationLog;

import java.util.List;

/**
 * 操作日志服务
 *
 * @author tv_recru
 */
public interface LogService extends IService<OperationLog> {

    /**
     * 分页查询日志
     *
     * @param pageNum       页码
     * @param pageSize      每页大小
     * @param userName       用户名（模糊查询）
     * @param operationType 操作类型
     * @param startDate      开始日期
     * @param endDate        结束日期
     * @return 分页结果
     */
    Page<OperationLog> page(Integer pageNum, Integer pageSize, String userName, String operationType, String startDate, String endDate);

    /**
     * 获取操作类型列表
     *
     * @return 操作类型列表
     */
    List<OperationTypeResponse> getTypes();

    /**
     * 清空所有日志
     */
    void clear();
}
