package com.tv.recruitment.service;

import java.util.List;
import java.util.Map;

/**
 * 待注册设备服务
 */
public interface PendingDeviceService {

    /**
     * 添加待注册设备
     * @param deviceCode 设备编码
     * @param deviceInfo 设备信息（IP、连接时间等）
     */
    void addPendingDevice(String deviceCode, Map<String, Object> deviceInfo);

    /**
     * 移除待注册设备
     * @param deviceCode 设备编码
     */
    void removePendingDevice(String deviceCode);

    /**
     * 获取所有待注册设备
     * @return 待注册设备列表
     */
    List<Map<String, Object>> getPendingDevices();

    /**
     * 检查设备是否已注册
     * @param deviceCode 设备编码
     * @return true-已注册, false-未注册
     */
    boolean isRegistered(String deviceCode);
}