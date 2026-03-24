package com.tv.recruitment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tv.recruitment.common.result.Result;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 海报控制器
 */
@Tag(name = "海报管理")
@RestController
@RequestMapping("/posters")
@RequiredArgsConstructor
public class PosterController {

    private final PosterMapper posterMapper;
    private final PosterTemplateMapper templateMapper;
    private final JobMapper jobMapper;
    private final FileStorageService fileStorageService;
    private final PosterGenerateService posterGenerateService;

    @Operation(summary = "获取海报模板列表")
    @GetMapping("/templates")
    public Result<List<PosterTemplate>> getTemplates() {
        List<PosterTemplate> templates = templateMapper.selectList(
                new LambdaQueryWrapper<PosterTemplate>()
                        .eq(PosterTemplate::getStatus, 1)
                        .orderByDesc(PosterTemplate::getIsDefault)
        );
        return Result.success(templates);
    }

    @Operation(summary = "分页查询海报")
    @GetMapping
    public Result<Page<PosterResponse>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String posterName) {
        Page<Poster> page = new Page<>(pageNum, pageSize);
        Page<Poster> result = posterMapper.selectPage(page,
                new LambdaQueryWrapper<Poster>()
                        .like(posterName != null && !posterName.isEmpty(), Poster::getPosterName, posterName)
                        .orderByDesc(Poster::getCreateTime));

        // 转换为响应对象
        Page<PosterResponse> responsePage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<PosterResponse> records = result.getRecords().stream().map(this::convertToResponse).collect(Collectors.toList());
        responsePage.setRecords(records);

        return Result.success(responsePage);
    }

    @Operation(summary = "获取海报详情")
    @GetMapping("/{id}")
    public Result<PosterResponse> getById(@PathVariable Long id) {
        Poster poster = posterMapper.selectById(id);
        if (poster == null) {
            return Result.success(null);
        }
        return Result.success(convertToResponse(poster));
    }

    @Operation(summary = "生成海报")
    @PostMapping
    public Result<Poster> generate(@RequestBody Poster poster) {
        // 使用海报生成服务生成海报
        String filePath = posterGenerateService.generateFromJob(poster);
        poster.setFilePath(filePath);
        posterMapper.insert(poster);
        return Result.success(poster);
    }

    @Operation(summary = "预览海报")
    @PostMapping("/preview")
    public Result<String> preview(@RequestBody Map<String, Object> data) {
        Long jobId = Long.valueOf(data.get("jobId").toString());
        Long templateId = data.get("templateId") != null ? Long.valueOf(data.get("templateId").toString()) : null;

        // 获取职位信息
        Job job = jobMapper.selectById(jobId);
        if (job == null) {
            return Result.success("/files/posters/preview.png");
        }

        // 构建数据
        Map<String, String> posterData = new HashMap<>();
        posterData.put("jobTitle", job.getJobName() != null ? job.getJobName() : "职位名称");
        posterData.put("company", job.getCompany() != null ? job.getCompany() : "公司名称");
        posterData.put("salary", FormatUtils.formatSalary(job.getSalaryMin(), job.getSalaryMax()));
        posterData.put("location", job.getWorkAddress() != null ? job.getWorkAddress() : "不限");
        posterData.put("education", job.getEducation() != null ? job.getEducation() : "不限");
        posterData.put("experience", job.getExperience() != null ? job.getExperience() : "不限");
        posterData.put("contactName", job.getContactName() != null ? job.getContactName() : "");
        posterData.put("contactPhone", job.getContactPhone() != null ? job.getContactPhone() : "");

        // 生成预览海报
        String previewPath = posterGenerateService.generatePoster(templateId, posterData);
        return Result.success(previewPath);
    }

    @Operation(summary = "生成海报(别名)")
    @PostMapping("/generate")
    public Result<PosterResponse> generateAlias(@RequestBody Poster poster) {
        // 使用海报生成服务生成海报
        String filePath = posterGenerateService.generateFromJob(poster);
        poster.setFilePath(filePath);
        posterMapper.insert(poster);
        return Result.success(convertToResponse(poster));
    }

    @Operation(summary = "批量生成海报")
    @PostMapping("/batch")
    public Result<List<Poster>> batchGenerate(@RequestBody Map<String, Object> data) {
        List<Long> jobIds = (List<Long>) data.get("jobIds");
        Long templateId = data.get("templateId") != null ? Long.valueOf(data.get("templateId").toString()) : null;

        List<Poster> posters = new ArrayList<>();
        for (Long jobId : jobIds) {
            Poster poster = new Poster();
            poster.setJobId(jobId);
            poster.setTemplateId(templateId);
            poster.setPosterName("海报_" + jobId);

            // 使用海报生成服务生成海报
            String filePath = posterGenerateService.generateFromJob(poster);
            poster.setFilePath(filePath);

            posterMapper.insert(poster);
            posters.add(poster);
        }
        return Result.success(posters);
    }

    /**
     * 格式化薪资
     */
    private String formatSalary(Integer salaryMin, Integer salaryMax) {
        return FormatUtils.formatSalary(salaryMin, salaryMax);
    }

    @Operation(summary = "导出海报")
    @GetMapping("/{id}/export")
    public void export(@PathVariable Long id, @RequestParam(defaultValue = "png") String format,
                       HttpServletResponse response) {
        Poster poster = posterMapper.selectById(id);
        if (poster == null || poster.getFilePath() == null || poster.getFilePath().isEmpty()) {
            FileDownloadUtils.writeErrorResponse(response, 404, "海报不存在");
            return;
        }

        try {
            // 加载文件
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

    @Operation(summary = "批量导出海报")
    @PostMapping("/export")
    public void batchExport(@RequestBody Map<String, Object> data,
                            HttpServletResponse response) {
        List<Long> ids = (List<Long>) data.get("ids");
        String format = (String) data.getOrDefault("format", "png");

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

    @Operation(summary = "编辑海报")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Poster poster) {
        poster.setId(id);
        posterMapper.updateById(poster);
        return Result.success();
    }

    @Operation(summary = "删除海报")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        posterMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 转换为响应对象
     */
    private PosterResponse convertToResponse(Poster poster) {
        PosterResponse response = new PosterResponse();
        response.setId(poster.getId());
        response.setPosterName(poster.getPosterName());
        response.setJobId(poster.getJobId());
        response.setTemplateId(poster.getTemplateId());
        response.setFilePath(poster.getFilePath());
        response.setFileSize(poster.getFileSize());
        response.setCreateTime(poster.getCreateTime());

        // 查询职位名称
        if (poster.getJobId() != null) {
            Job job = jobMapper.selectById(poster.getJobId());
            if (job != null) {
                response.setJobName(job.getJobName());
            }
        }

        // 查询模板名称
        if (poster.getTemplateId() != null) {
            PosterTemplate template = templateMapper.selectById(poster.getTemplateId());
            if (template != null) {
                response.setTemplateName(template.getTemplateName());
            }
        }

        return response;
    }
}