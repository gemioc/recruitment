package com.tv.recruitment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tv.recruitment.entity.Image;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 图片服务
 *
 * @author tv_recru
 */
public interface ImageService extends IService<Image> {

    /**
     * 分页查询图片
     *
     * @param pageNum    页码
     * @param pageSize   每页大小
     * @param imageName  图片名称（模糊查询）
     * @return 分页结果
     */
    Page<Image> page(Integer pageNum, Integer pageSize, String imageName);

    /**
     * 上传图片文件
     *
     * @param file 图片文件
     * @return 上传结果 {filePath, fileSize, imageName}
     */
    Map<String, Object> uploadImage(MultipartFile file);

    /**
     * 创建图片记录
     *
     * @param image 图片信息
     * @return 创建后的图片
     */
    Image createImage(Image image);

    /**
     * 更新图片信息
     *
     * @param id    图片ID
     * @param image 图片信息
     */
    void updateImage(Long id, Image image);

    /**
     * 删除图片
     *
     * @param id 图片ID
     */
    void deleteImage(Long id);

    /**
     * 置顶/取消置顶图片
     *
     * @param id    图片ID
     * @param isTop 是否置顶
     */
    void setTop(Long id, Integer isTop);

    /**
     * 获取图片访问地址
     *
     * @param id 图片ID
     * @return 访问地址
     */
    String getImageUrl(Long id);
}