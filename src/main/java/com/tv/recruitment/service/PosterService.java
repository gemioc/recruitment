package com.tv.recruitment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.tv.recruitment.dto.response.PosterResponse;
import com.tv.recruitment.entity.Poster;
import com.tv.recruitment.entity.PosterTemplate;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;

/**
 * 海报服务
 *
 * @author tv_recru
 */
public interface PosterService extends IService<Poster> {

    /**
     * 获取海报模板列表
     *
     * @return 模板列表
     */
    List<PosterTemplate> getTemplates();

    /**
     * 分页查询海报
     *
     * @param pageNum    页码
     * @param pageSize   每页大小
     * @param posterName 海报名称（模糊查询）
     * @return 分页结果
     */
    Page<PosterResponse> page(Integer pageNum, Integer pageSize, String posterName);

    /**
     * 获取海报详情
     *
     * @param id 海报ID
     * @return 海报响应对象
     */
    PosterResponse getById(Long id);

    /**
     * 生成海报
     *
     * @param poster 海报信息
     * @return 生成的海报
     */
    Poster generate(Poster poster);

    /**
     * 预览海报
     *
     * @param data 预览参数 {jobId, templateId}
     * @return 预览路径
     */
    String preview(Map<String, Object> data);

    /**
     * 生成海报（别名）
     *
     * @param poster 海报信息
     * @return 海报响应对象
     */
    PosterResponse generateAlias(Poster poster);

    /**
     * 批量生成海报
     *
     * @param jobIds     职位ID列表
     * @param templateId 模板ID
     * @param svgContent SVG内容
     * @param posterName 海报名称
     * @return 生成的海报列表
     */
    List<Poster> batchGenerate(List<Long> jobIds, Long templateId, String svgContent, String posterName) throws JsonProcessingException;

    /**
     * 格式化薪资
     *
     * @param salaryMin 最低薪资
     * @param salaryMax 最高薪资
     * @return 格式化后的薪资字符串
     */
    String formatSalary(Integer salaryMin, Integer salaryMax);

    /**
     * 导出海报
     *
     * @param id       海报ID
     * @param format   导出格式
     * @param response 响应对象
     */
    void export(Long id, String format, HttpServletResponse response);

    /**
     * 批量导出海报
     *
     * @param ids      海报ID列表
     * @param format   导出格式
     * @param response 响应对象
     */
    void batchExport(List<Long> ids, String format, HttpServletResponse response);

    /**
     * 更新海报
     *
     * @param id     海报ID
     * @param poster 海报信息
     */
    void update(Long id, Poster poster);

    /**
     * 删除海报
     *
     * @param id 海报ID
     */
    void delete(Long id);

    /**
     * 转换为响应对象
     *
     * @param poster 海报实体
     * @return 海报响应对象
     */
    PosterResponse convertToResponse(Poster poster);
}
