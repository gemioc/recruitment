package com.tv.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tv.recruitment.entity.Image;
import com.tv.recruitment.mapper.ImageMapper;
import com.tv.recruitment.service.FileStorageService;
import com.tv.recruitment.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 图片服务实现
 *
 * @author tv_recru
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceImpl extends ServiceImpl<ImageMapper, Image> implements ImageService {

    private final ImageMapper imageMapper;
    private final FileStorageService fileStorageService;

    @Override
    public Page<Image> page(Integer pageNum, Integer pageSize, String imageName) {
        Page<Image> page = new Page<>(pageNum, pageSize);
        return imageMapper.selectPage(page,
                new LambdaQueryWrapper<Image>()
                        .like(imageName != null, Image::getImageName, imageName)
                        .orderByDesc(Image::getIsTop)
                        .orderByDesc(Image::getCreateTime));
    }

    @Override
    public Map<String, Object> uploadImage(MultipartFile file) {
        // 保存文件
        String filePath = fileStorageService.saveFile(file, "images");

        Map<String, Object> result = new HashMap<>();
        result.put("filePath", filePath);
        result.put("fileSize", file.getSize());
        result.put("imageName", file.getOriginalFilename());

        return result;
    }

    @Override
    public Image createImage(Image image) {
        imageMapper.insert(image);
        return image;
    }

    @Override
    public void updateImage(Long id, Image image) {
        image.setId(id);
        imageMapper.updateById(image);
    }

    @Override
    public void deleteImage(Long id) {
        Image image = imageMapper.selectById(id);
        if (image != null) {
            fileStorageService.deleteFile(image.getFilePath());
            if (image.getThumbnailPath() != null) {
                fileStorageService.deleteFile(image.getThumbnailPath());
            }
            imageMapper.deleteById(id);
        }
    }

    @Override
    public void setTop(Long id, Integer isTop) {
        Image image = new Image();
        image.setId(id);
        image.setIsTop(isTop);
        imageMapper.updateById(image);
    }

    @Override
    public String getImageUrl(Long id) {
        Image image = imageMapper.selectById(id);
        if (image != null) {
            return fileStorageService.getFileUrl(image.getFilePath());
        }
        return null;
    }
}