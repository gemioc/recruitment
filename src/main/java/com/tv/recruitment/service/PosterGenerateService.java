package com.tv.recruitment.service;

import com.tv.recruitment.entity.Poster;

import java.util.Map;

/**
 * 海报生成服务接口
 */
public interface PosterGenerateService {

    /**
     * 根据模板生成海报SVG
     * @param templateId 模板ID
     * @param data 数据映射
     * @return 生成的海报文件路径
     */
    String generatePoster(Long templateId, Map<String, String> data);

    /**
     * 根据职位信息生成海报
     * @param poster 海报实体
     * @return 生成的海报文件路径
     */
    String generateFromJob(Poster poster);
}