package com.tv.recruitment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tv.recruitment.entity.DeviceGroup;

import java.util.List;

/**
 * 设备分组服务接口
 *
 * @author tv_recru
 */
public interface DeviceGroupService extends IService<DeviceGroup> {

    /**
     * 获取分组列表（包含设备数量和在线数量统计）
     *
     * @return 分组列表
     */
    List<DeviceGroup> listWithDeviceCount();

    /**
     * 获取分组详情
     *
     * @param id 分组ID
     * @return 分组信息
     */
    DeviceGroup getDetailById(Long id);

    /**
     * 新增分组
     *
     * @param group 分组信息
     */
    void createGroup(DeviceGroup group);

    /**
     * 更新分组
     *
     * @param id    分组ID
     * @param group 分组信息
     */
    void updateGroup(Long id, DeviceGroup group);

    /**
     * 删除分组
     *
     * @param id 分组ID
     */
    void deleteGroup(Long id);
}
