package com.tv.recruitment.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.read.listener.ReadListener;
import com.tv.recruitment.entity.Job;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 职位导入监听器
 *
 * @author tv_recru
 */
@Slf4j
@Getter
public class JobImportListener implements ReadListener<Job> {

    private final List<Job> successData = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();

    @Override
    public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
        log.info("检测到表头: {}, 总列数: {}", headMap, headMap.size());
    }

    @Override
    public void invoke(Job data, AnalysisContext context) {
        int rowNum = context.readRowHolder().getRowIndex() + 1;
        log.info("第{}行数据: {}", rowNum, data);
        String rowError = validateData(data, rowNum);
        if (rowError != null) {
            log.warn("第{}行校验失败: {}", rowNum, rowError);
            errors.add(rowError);
            return;
        }
        successData.add(data);
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) {
        int rowNum = context.readRowHolder().getRowIndex() + 1;
        log.error("第{}行解析异常: {}", rowNum, exception.getMessage(), exception);
        errors.add("第" + rowNum + "行：解析失败");
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 解析完成
    }

    private String validateData(Job data, int rowNum) {
        if (data.getJobName() == null || data.getJobName().trim().isEmpty()) {
            return "第" + rowNum + "行：职位名称不能为空";
        }
        if (data.getCompany() == null || data.getCompany().trim().isEmpty()) {
            return "第" + rowNum + "行：公司名称不能为空";
        }
        if (data.getWorkAddress() == null || data.getWorkAddress().trim().isEmpty()) {
            return "第" + rowNum + "行：工作地点不能为空";
        }
        if (data.getContactName() == null || data.getContactName().trim().isEmpty()) {
            return "第" + rowNum + "行：联系人不能为空";
        }
        if (data.getContactPhone() == null || data.getContactPhone().trim().isEmpty()) {
            return "第" + rowNum + "行：联系电话不能为空";
        }
        // 薪资校验：都为0视为面议通过；只有一个值也通过；两者都不为0时上限必须大于下限
        Integer salaryMin = data.getSalaryMin();
        Integer salaryMax = data.getSalaryMax();
        if (salaryMin != null && salaryMax != null && salaryMin != 0 && salaryMax != 0) {
            if (salaryMax <= salaryMin) {
                return "第" + rowNum + "行：薪资上限必须大于下限";
            }
        }
        return null;
    }
}
