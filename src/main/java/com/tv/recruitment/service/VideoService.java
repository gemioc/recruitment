package com.tv.recruitment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tv.recruitment.entity.Video;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 视频服务
 *
 * @author tv_recru
 */
public interface VideoService extends IService<Video> {

    /**
     * 分页查询视频
     *
     * @param pageNum    页码
     * @param pageSize   每页大小
     * @param videoName  视频名称（模糊查询）
     * @return 分页结果
     */
    Page<Video> page(Integer pageNum, Integer pageSize, String videoName);

    /**
     * 上传视频文件
     *
     * @param file 视频文件
     * @return 上传结果 {filePath, fileSize, videoName}
     */
    Map<String, Object> uploadVideo(MultipartFile file);

    /**
     * 创建视频记录
     *
     * @param video 视频信息
     * @return 创建后的视频
     */
    Video createVideo(Video video);

    /**
     * 更新视频信息
     *
     * @param id    视频ID
     * @param video 视频信息
     */
    void updateVideo(Long id, Video video);

    /**
     * 删除视频
     *
     * @param id 视频ID
     */
    void deleteVideo(Long id);

    /**
     * 置顶/取消置顶视频
     *
     * @param id     视频ID
     * @param isTop  是否置顶
     */
    void setTop(Long id, Integer isTop);

    /**
     * 获取视频播放地址
     *
     * @param id 视频ID
     * @return 播放地址
     */
    String getPlayUrl(Long id);
}
