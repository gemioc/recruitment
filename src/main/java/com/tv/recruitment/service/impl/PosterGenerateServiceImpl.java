package com.tv.recruitment.service.impl;

import com.tv.recruitment.entity.Job;
import com.tv.recruitment.entity.Poster;
import com.tv.recruitment.entity.PosterTemplate;
import com.tv.recruitment.common.utils.FormatUtils;
import com.tv.recruitment.mapper.JobMapper;
import com.tv.recruitment.mapper.PosterTemplateMapper;
import com.tv.recruitment.service.FileStorageService;
import com.tv.recruitment.service.PosterGenerateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 海报生成服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PosterGenerateServiceImpl implements PosterGenerateService {

    private final PosterTemplateMapper templateMapper;
    private final JobMapper jobMapper;
    private final FileStorageService fileStorageService;

    @Value("${file.storage.base-path:D:/B-code_space/tv-files}")
    private String basePath;

    @Override
    public String generatePoster(Long templateId, Map<String, String> data) {
        // 获取模板
        PosterTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new RuntimeException("模板不存在");
        }

        String templatePath = template.getTemplatePath();
        if (templatePath == null || templatePath.isEmpty()) {
            // 使用默认模板
            templatePath = "/templates/template-blue.svg";
        }

        // 读取模板内容
        String templateContent = readTemplate(templatePath);

        // 替换占位符
        String posterContent = replacePlaceholders(templateContent, data);

        // 生成文件名
        String fileName = "poster_" + System.currentTimeMillis() + ".svg";
        String filePath = "/posters/" + fileName;

        // 保存文件
        savePoster(filePath, posterContent);

        return filePath;
    }

    @Override
    public String generateFromJob(Poster poster) {
        // 获取职位信息
        Job job = null;
        Map<String, String> data = new HashMap<>();

        if (poster.getJobId() != null) {
            job = jobMapper.selectById(poster.getJobId());
        }

        if (job != null) {
            // 从职位信息填充数据
            data.put("jobTitle", job.getJobName() != null ? job.getJobName() : "职位名称");
            data.put("company", job.getCompany() != null ? job.getCompany() : "公司名称");
            data.put("salary", FormatUtils.formatSalary(job.getSalaryMin(), job.getSalaryMax()));
            data.put("location", job.getWorkAddress() != null ? job.getWorkAddress() : "面议");
            data.put("education", job.getEducation() != null ? job.getEducation() : "不限");
            data.put("experience", job.getExperience() != null ? job.getExperience() : "不限");
            data.put("contactName", job.getContactName() != null ? job.getContactName() : "");
            data.put("contactPhone", job.getContactPhone() != null ? job.getContactPhone() : "");
        } else {
            // 使用默认值
            data.put("jobTitle", "职位名称");
            data.put("company", "公司名称");
            data.put("salary", "面议");
            data.put("location", "不限");
            data.put("education", "不限");
            data.put("experience", "不限");
            data.put("contactName", "");
            data.put("contactPhone", "");
        }

        Long templateId = poster.getTemplateId();
        if (templateId == null) {
            // 使用默认模板
            return generateWithDefaultTemplate(data);
        }

        return generatePoster(templateId, data);
    }

    /**
     * 使用默认模板生成海报
     */
    private String generateWithDefaultTemplate(Map<String, String> data) {
        String templatePath = "/templates/template-blue.svg";
        String templateContent = readTemplate(templatePath);
        String posterContent = replacePlaceholders(templateContent, data);

        String fileName = "poster_" + System.currentTimeMillis() + ".svg";
        String filePath = "/posters/" + fileName;

        savePoster(filePath, posterContent);

        return filePath;
    }

    /**
     * 读取模板文件
     */
    private String readTemplate(String templatePath) {
        try {
            // 模板路径格式: /templates/template-blue.svg
            // 实际文件位置: basePath + /templates/template-blue.svg
            Path path = Paths.get(basePath, templatePath);
            if (Files.exists(path)) {
                return Files.readString(path, StandardCharsets.UTF_8);
            }

            // 返回默认模板
            log.warn("模板文件不存在: {}, 使用默认模板", path);
            return getDefaultTemplate();
        } catch (IOException e) {
            log.error("读取模板文件失败: {}", e.getMessage());
            return getDefaultTemplate();
        }
    }

    /**
     * 替换占位符
     */
    private String replacePlaceholders(String template, Map<String, String> data) {
        String result = template;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue() : "";
            result = result.replace(placeholder, value);
        }
        return result;
    }

    /**
     * 保存海报文件
     */
    private void savePoster(String filePath, String content) {
        try {
            Path path = Paths.get(basePath, filePath);
            Files.createDirectories(path.getParent());
            Files.writeString(path, content, StandardCharsets.UTF_8);
            log.info("海报生成成功: {}", filePath);
        } catch (IOException e) {
            log.error("保存海报文件失败: {}", e.getMessage());
            throw new RuntimeException("保存海报文件失败: " + e.getMessage());
        }
    }

    /**
     * 获取默认模板
     */
    private String getDefaultTemplate() {
        return """
            <svg width="1920" height="1080" viewBox="0 0 1920 1080" xmlns="http://www.w3.org/2000/svg">
              <defs>
                <linearGradient id="bg-gradient" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" style="stop-color:#1e3c72"/>
                  <stop offset="100%" style="stop-color:#2a5298"/>
                </linearGradient>
              </defs>
              <rect width="1920" height="1080" fill="url(#bg-gradient)"/>
              <circle cx="1700" cy="150" r="200" fill="rgba(255,255,255,0.05)"/>
              <circle cx="200" cy="900" r="150" fill="rgba(255,255,255,0.05)"/>
              <text x="960" y="120" font-family="Microsoft YaHei, sans-serif" font-size="48" fill="rgba(255,255,255,0.9)" text-anchor="middle">诚聘英才</text>
              <line x1="760" y1="150" x2="1160" y2="150" stroke="rgba(255,255,255,0.5)" stroke-width="2"/>
              <text x="960" y="280" font-family="Microsoft YaHei, sans-serif" font-size="72" fill="#fff" text-anchor="middle" font-weight="bold">{{jobTitle}}</text>
              <text x="960" y="380" font-family="Microsoft YaHei, sans-serif" font-size="42" fill="rgba(255,255,255,0.9)" text-anchor="middle">{{company}}</text>
              <rect x="710" y="430" width="500" height="80" rx="10" fill="rgba(245,108,108,0.9)"/>
              <text x="960" y="485" font-family="Microsoft YaHei, sans-serif" font-size="48" fill="#fff" text-anchor="middle" font-weight="bold">{{salary}}</text>
              <rect x="360" y="560" width="1200" height="280" rx="15" fill="rgba(255,255,255,0.1)"/>
              <text x="960" y="620" font-family="Microsoft YaHei, sans-serif" font-size="28" fill="rgba(255,255,255,0.7)" text-anchor="middle">岗位要求</text>
              <g transform="translate(460, 680)">
                <rect x="0" y="0" width="240" height="60" rx="8" fill="rgba(255,255,255,0.15)"/>
                <text x="120" y="40" font-family="Microsoft YaHei, sans-serif" font-size="24" fill="#fff" text-anchor="middle">{{location}}</text>
                <text x="120" y="-10" font-family="Microsoft YaHei, sans-serif" font-size="16" fill="rgba(255,255,255,0.6)" text-anchor="middle">工作地点</text>
                <rect x="280" y="0" width="240" height="60" rx="8" fill="rgba(255,255,255,0.15)"/>
                <text x="400" y="40" font-family="Microsoft YaHei, sans-serif" font-size="24" fill="#fff" text-anchor="middle">{{education}}</text>
                <text x="400" y="-10" font-family="Microsoft YaHei, sans-serif" font-size="16" fill="rgba(255,255,255,0.6)" text-anchor="middle">学历要求</text>
                <rect x="560" y="0" width="240" height="60" rx="8" fill="rgba(255,255,255,0.15)"/>
                <text x="680" y="40" font-family="Microsoft YaHei, sans-serif" font-size="24" fill="#fff" text-anchor="middle">{{experience}}</text>
                <text x="680" y="-10" font-family="Microsoft YaHei, sans-serif" font-size="16" fill="rgba(255,255,255,0.6)" text-anchor="middle">经验要求</text>
              </g>
              <rect x="360" y="870" width="1200" height="150" rx="15" fill="rgba(255,255,255,0.15)"/>
              <text x="500" y="920" font-family="Microsoft YaHei, sans-serif" font-size="24" fill="rgba(255,255,255,0.7)">联系人：</text>
              <text x="600" y="920" font-family="Microsoft YaHei, sans-serif" font-size="24" fill="#fff">{{contactName}}</text>
              <text x="500" y="970" font-family="Microsoft YaHei, sans-serif" font-size="24" fill="rgba(255,255,255,0.7)">联系电话：</text>
              <text x="620" y="970" font-family="Microsoft YaHei, sans-serif" font-size="24" fill="#fff">{{contactPhone}}</text>
              <text x="960" y="1060" font-family="Microsoft YaHei, sans-serif" font-size="18" fill="rgba(255,255,255,0.4)" text-anchor="middle">期待您的加入</text>
            </svg>
            """;
    }
}