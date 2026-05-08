package com.tv.recruitment.service;

import com.tv.recruitment.entity.Poster;

import java.util.Map;

/**
 * 海报生成服务接口
 */
public interface PosterGenerateService {

    /**
     * 根据模板生成海报PNG
     * @deprecated 此方法已废弃，前端不再调用，请使用 generateFromSvg
     * @param templateId 模板ID
     * @param data 数据映射
     * @return 生成的海报文件路径
     */
    @Deprecated
    String generatePoster(Long templateId, Map<String, String> data);

    /**
     * 根据职位信息生成海报
     * @deprecated 此方法已废弃，前端不再调用，请使用 generateFromSvg
     * @param poster 海报实体
     * @return 生成的海报文件路径
     */
    @Deprecated
    String generateFromJob(Poster poster);

    /**
     * 直接使用SVG内容生成海报PNG（当前唯一在用）
     * @param svgContent SVG内容
     * @return 生成的海报文件路径
     */
    String generateFromSvg(String svgContent);
}
