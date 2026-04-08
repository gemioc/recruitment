package com.tv.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tv.recruitment.dto.response.OperationTypeResponse;
import com.tv.recruitment.entity.OperationLog;
import com.tv.recruitment.mapper.OperationLogMapper;
import com.tv.recruitment.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 操作日志服务实现
 *
 * @author tv_recru
 */
@Slf4j
@Service
public class LogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements LogService {

    @Override
    public Page<OperationLog> page(Integer pageNum, Integer pageSize, String userName, String operationType, String startDate, String endDate) {
        Page<OperationLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<OperationLog>()
                .like(userName != null && !userName.isEmpty(), OperationLog::getUserName, userName)
                .eq(operationType != null && !operationType.isEmpty(), OperationLog::getOperationType, operationType)
                .ge(startDate != null && !startDate.isEmpty(), OperationLog::getOperationTime, startDate + " 00:00:00")
                .le(endDate != null && !endDate.isEmpty(), OperationLog::getOperationTime, endDate + " 23:59:59")
                .orderByDesc(OperationLog::getOperationTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public List<OperationTypeResponse> getTypes() {
        return List.of(
                new OperationTypeResponse("LOGIN", "用户登录"),
                new OperationTypeResponse("LOGOUT", "用户退出"),
                new OperationTypeResponse("UPDATE_PASSWORD", "修改密码"),
                new OperationTypeResponse("CREATE_JOB", "新增职位"),
                new OperationTypeResponse("UPDATE_JOB", "编辑职位"),
                new OperationTypeResponse("DELETE_JOB", "删除职位"),
                new OperationTypeResponse("UPDATE_JOB_STATUS", "更改职位状态"),
                new OperationTypeResponse("BATCH_UPDATE_JOB_STATUS", "批量更改职位状态"),
                new OperationTypeResponse("CREATE_POSTER", "生成海报"),
                new OperationTypeResponse("BATCH_CREATE_POSTER", "批量生成海报"),
                new OperationTypeResponse("DELETE_POSTER", "删除海报"),
                new OperationTypeResponse("PUSH_POSTER", "推送海报"),
                new OperationTypeResponse("PUSH_VIDEO", "推送视频"),
                new OperationTypeResponse("PUSH_CONTENT", "批量推送内容"),
                new OperationTypeResponse("DEVICE_CONTROL", "设备控制")
        );
    }

    @Override
    public void clear() {
        baseMapper.delete(null);
    }
}
