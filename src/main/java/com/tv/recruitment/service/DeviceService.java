package com.tv.recruitment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tv.recruitment.entity.Device;

import java.util.Map;

/**
 * 设备服务
 */
public interface DeviceService extends IService<Device> {

    /**
     * 分页查询设备
     */
    Page<Device> page(Integer pageNum, Integer pageSize, String deviceCode, String deviceName,
                      String location, Long groupId, Integer onlineStatus, Integer status);

    /**
     * 获取监控统计
     */
    Map<String, Object> getMonitor();

    /**
     * 更新在线状态
     */
    void updateOnlineStatus(String deviceCode, boolean online);
}