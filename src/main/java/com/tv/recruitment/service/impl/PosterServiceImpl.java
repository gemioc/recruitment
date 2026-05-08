package com.tv.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tv.recruitment.common.utils.FileDownloadUtils;
import com.tv.recruitment.common.utils.FormatUtils;
import com.tv.recruitment.dto.response.PosterResponse;
import com.tv.recruitment.entity.Job;
import com.tv.recruitment.entity.Poster;
import com.tv.recruitment.entity.PosterTemplate;
import com.tv.recruitment.mapper.JobMapper;
import com.tv.recruitment.mapper.PosterMapper;
import com.tv.recruitment.mapper.PosterTemplateMapper;
import com.tv.recruitment.service.FileStorageService;
import com.tv.recruitment.service.PosterGenerateService;
import com.tv.recruitment.service.PosterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import jakarta.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 海报服务实现
 *
 * @author tv_recru
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PosterServiceImpl extends ServiceImpl<PosterMapper, Poster> implements PosterService {

    private final PosterMapper posterMapper;
    private final PosterTemplateMapper templateMapper;
    private final JobMapper jobMapper;
    private final FileStorageService fileStorageService;
    private final PosterGenerateService posterGenerateService;
    private final ObjectMapper objectMapper;

    @Value("${file.storage.base-path:D:/B-code_space/tv-files}")
    private String basePath;

    @Override
    public List<PosterTemplate> getTemplates() {
        return templateMapper.selectList(
                new LambdaQueryWrapper<PosterTemplate>()
                        .eq(PosterTemplate::getStatus, 1)
                        .orderByDesc(PosterTemplate::getIsDefault)
        );
    }

    @Override
    public Page<PosterResponse> page(Integer pageNum, Integer pageSize, String posterName) {
        Page<Poster> page = new Page<>(pageNum, pageSize);
        Page<Poster> result = posterMapper.selectPage(page,
                new LambdaQueryWrapper<Poster>()
                        .like(posterName != null && !posterName.isEmpty(), Poster::getPosterName, posterName)
                        .orderByDesc(Poster::getCreateTime));

        Page<PosterResponse> responsePage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<PosterResponse> records = result.getRecords().stream().map(this::convertToResponse).collect(Collectors.toList());
        responsePage.setRecords(records);

        return responsePage;
    }

    @Override
    public PosterResponse getById(Long id) {
        Poster poster = posterMapper.selectById(id);
        if (poster == null) {
            return null;
        }
        return convertToResponse(poster);
    }

    @Override
    public Poster generate(Poster poster) {
        // 前端已改用 generateAlias + svgContent 方式，此方法仅作兼容保留
        if (poster.getSvgContent() != null && !poster.getSvgContent().isEmpty()) {
            String filePath = posterGenerateService.generateFromSvg(poster.getSvgContent());
            poster.setFilePath(filePath);
        }
        posterMapper.insert(poster);
        return poster;
    }

    @Override
    @Deprecated
    public String preview(Map<String, Object> data) {
        // 前端已改用纯前端SVG渲染，不再调用此方法
        // 此方法仅作兼容保留
        return "/files/posters/preview.png";
    }

    @Override
    public PosterResponse generateAlias(Poster poster) {
        String filePath;

        // 如果前端提供了SVG内容，直接使用
        if (poster.getSvgContent() != null && !poster.getSvgContent().isEmpty()) {
            filePath = posterGenerateService.generateFromSvg(poster.getSvgContent());
        } else {
            // 否则根据模板和职位信息生成
            filePath = posterGenerateService.generateFromJob(poster);
        }

        poster.setFilePath(filePath);
        posterMapper.insert(poster);
        return convertToResponse(poster);
    }

    @Override
    public List<Poster> batchGenerate(List<Long> jobIds, Long templateId, String svgContent, String posterName) throws JsonProcessingException {
        List<Poster> posters = new ArrayList<>();

        // 如果提供了svgContent（多岗位海报），只生成一张
        if (svgContent != null && !svgContent.isEmpty()) {
            Poster poster = new Poster();
            // jobIds中的元素从JSON反序列化时可能是Integer，统一转为Long后存入jobId
            if (jobIds != null && !jobIds.isEmpty()) {
                Object firstId = jobIds.get(0);
                if (firstId instanceof Long) {
                    poster.setJobId((Long) firstId);
                } else if (firstId instanceof Integer) {
                    poster.setJobId(((Integer) firstId).longValue());
                }
                // 保存所有岗位ID为JSON格式
                poster.setJobIds(objectMapper.writeValueAsString(jobIds));
            }
            poster.setTemplateId(templateId);
            poster.setPosterName(posterName != null && !posterName.isEmpty() ? posterName : "多岗位招聘海报");
            poster.setSvgContent(svgContent);

            String filePath = posterGenerateService.generateFromSvg(svgContent);
            poster.setFilePath(filePath);

            posterMapper.insert(poster);
            posters.add(poster);
            return posters;
        }

        // 原有逻辑：逐个生成单岗位海报
        for (Long jobId : jobIds) {
            Poster poster = new Poster();
            poster.setJobId(jobId);
            poster.setTemplateId(templateId);
            poster.setPosterName("海报_" + jobId);

            String filePath = posterGenerateService.generateFromJob(poster);
            poster.setFilePath(filePath);

            posterMapper.insert(poster);
            posters.add(poster);
        }
        return posters;
    }

    @Override
    public String formatSalary(Integer salaryMin, Integer salaryMax) {
        return FormatUtils.formatSalary(salaryMin, salaryMax);
    }

    @Override
    public void export(Long id, String format, HttpServletResponse response) {
        Poster poster = posterMapper.selectById(id);
        if (poster == null || poster.getFilePath() == null || poster.getFilePath().isEmpty()) {
            FileDownloadUtils.writeErrorResponse(response, 404, "海报不存在");
            return;
        }

        try {
            Resource resource = fileStorageService.loadFile(poster.getFilePath());
            String fileName = poster.getPosterName() + "." + format;
            FileDownloadUtils.setDownloadHeaders(response, fileName);

            try (InputStream is = resource.getInputStream()) {
                StreamUtils.copy(is, response.getOutputStream());
                response.getOutputStream().flush();
            }
        } catch (Exception e) {
            FileDownloadUtils.writeErrorResponse(response, 500, "导出失败: " + e.getMessage());
        }
    }

    @Override
    public void batchExport(List<Long> ids, String format, HttpServletResponse response) {
        if (ids == null || ids.isEmpty()) {
            FileDownloadUtils.writeErrorResponse(response, 400, "请选择要导出的海报");
            return;
        }

        try {
            FileDownloadUtils.setZipDownloadHeaders(response, "posters");

            java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(response.getOutputStream());

            for (Long id : ids) {
                Poster poster = posterMapper.selectById(id);
                if (poster != null && poster.getFilePath() != null && !poster.getFilePath().isEmpty()) {
                    try {
                        Resource resource = fileStorageService.loadFile(poster.getFilePath());
                        java.util.zip.ZipEntry entry = new java.util.zip.ZipEntry(
                                poster.getPosterName() + "_" + id + "." + format);
                        zos.putNextEntry(entry);
                        try (InputStream is = resource.getInputStream()) {
                            StreamUtils.copy(is, zos);
                        }
                        zos.closeEntry();
                    } catch (Exception e) {
                        // 跳过无法读取的文件
                    }
                }
            }

            zos.finish();
            zos.flush();
        } catch (Exception e) {
            FileDownloadUtils.writeErrorResponse(response, 500, "导出失败");
        }
    }

    @Override
    public void update(Long id, Poster poster) {
        poster.setId(id);
        posterMapper.updateById(poster);
    }

    @Override
    public void delete(Long id) {
        posterMapper.deleteById(id);
    }

    @Override
    public Poster uploadPng(String pngBase64, String posterName, Long templateId, List<Long> jobIds) {
        Poster poster = new Poster();
        poster.setTemplateId(templateId);
        poster.setPosterName(posterName != null && !posterName.isEmpty() ? posterName : "海报");
        if (jobIds != null && !jobIds.isEmpty()) {
            try {
                poster.setJobIds(objectMapper.writeValueAsString(jobIds));
                Object firstId = jobIds.get(0);
                if (firstId instanceof Long) {
                    poster.setJobId((Long) firstId);
                } else if (firstId instanceof Integer) {
                    poster.setJobId(((Integer) firstId).longValue());
                }
            } catch (JsonProcessingException e) {
                log.error("序列化jobIds失败", e);
            }
        }

        // 保存PNG文件
        String fileName = "poster_" + System.currentTimeMillis() + ".png";
        String filePath = "/posters/" + fileName;

        try {
            // 解码Base64并保存
            byte[] pngBytes = java.util.Base64.getDecoder().decode(pngBase64);
            java.nio.file.Path path = java.nio.file.Paths.get(basePath, filePath);
            java.nio.file.Files.createDirectories(path.getParent());
            java.nio.file.Files.write(path, pngBytes);

            poster.setFilePath(filePath);
            posterMapper.insert(poster);

            log.info("PNG海报上传成功: {} ({} bytes)", filePath, pngBytes.length);
            return poster;
        } catch (Exception e) {
            log.error("保存PNG海报失败", e);
            throw new RuntimeException("保存PNG海报失败: " + e.getMessage());
        }
    }

    @Override
    public PosterResponse convertToResponse(Poster poster) {
        PosterResponse response = new PosterResponse();
        response.setId(poster.getId());
        response.setPosterName(poster.getPosterName());
        response.setJobId(poster.getJobId());
        response.setTemplateId(poster.getTemplateId());
        response.setFilePath(poster.getFilePath());
        response.setFileSize(poster.getFileSize());
        response.setCreateTime(poster.getCreateTime());

        // 处理多岗位海报
        if (poster.getJobIds() != null && !poster.getJobIds().isEmpty()) {
            response.setJobIds(poster.getJobIds());
            // 多岗位海报直接设置模板名称
            response.setTemplateName("多岗招聘");
            try {
                List<Long> jobIdList = objectMapper.readValue(poster.getJobIds(), new TypeReference<List<Long>>() {});
                List<String> jobNames = new ArrayList<>();
                for (Long jobId : jobIdList) {
                    Job job = jobMapper.selectById(jobId);
                    if (job != null) {
                        jobNames.add(job.getJobName());
                    }
                }
                response.setRelatedJobNames(String.join("、", jobNames));
                response.setJobName(String.join("、", jobNames));
            } catch (JsonProcessingException e) {
                log.error("解析jobIds失败", e);
            }
        } else if (poster.getJobId() != null) {
            Job job = jobMapper.selectById(poster.getJobId());
            if (job != null) {
                response.setJobName(job.getJobName());
            }
        }

        if (poster.getTemplateId() != null) {
            PosterTemplate template = templateMapper.selectById(poster.getTemplateId());
            if (template != null) {
                response.setTemplateName(template.getTemplateName());
            }
        }

        return response;
    }
}
