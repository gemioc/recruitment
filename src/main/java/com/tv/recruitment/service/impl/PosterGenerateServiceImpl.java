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
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * 海报生成服务实现
 * 生成 PNG 格式海报，兼容 Android Glide 加载
 * 支持竖版和横版两种模板
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

    // 横版尺寸 - 1920x1080 Full HD
    private static final float POSTER_WIDTH = 1920f;
    private static final float POSTER_HEIGHT = 1080f;

    @Override
    public String generatePoster(Long templateId, Map<String, String> data) {
        String templatePath = null;

        // 获取模板路径
        if (templateId != null) {
            PosterTemplate template = templateMapper.selectById(templateId);
            if (template != null) {
                templatePath = template.getTemplatePath();
            }
        }

        // 如果没有模板，使用默认模板
        if (templatePath == null || templatePath.isEmpty()) {
            templatePath = "/templates/template-business.svg";
        }

        // 读取模板内容
        String templateContent = readTemplate(templatePath);

        // 处理文本换行
        Map<String, String> processedData = processDataWithTextWrapping(data);

        // 替换占位符
        String svgContent = replacePlaceholders(templateContent, processedData);

        // 生成文件名 (PNG格式)
        String fileName = "poster_" + System.currentTimeMillis() + ".png";
        String filePath = "/posters/" + fileName;

        // 转换SVG为PNG并保存
        savePosterAsPng(filePath, svgContent);

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
            data.put("recruitCount", job.getRecruitCount() != null ? String.valueOf(job.getRecruitCount()) : "若干");
            // 岗位职责、任职要求、福利待遇 - 原始文本，后续处理换行
            data.put("responsibilities", job.getResponsibilities() != null ? job.getResponsibilities() : "面议");
            data.put("requirements", job.getRequirements() != null ? job.getRequirements() : "面议");
            data.put("welfare", job.getWelfare() != null ? job.getWelfare() : "面议");
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
            data.put("recruitCount", "若干");
            data.put("responsibilities", "面议");
            data.put("requirements", "面议");
            data.put("welfare", "面议");
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

    @Override
    public String generateFromSvg(String svgContent) {
        if (svgContent == null || svgContent.isEmpty()) {
            throw new IllegalArgumentException("SVG内容不能为空");
        }

        String fileName = "poster_" + System.currentTimeMillis() + ".png";
        String filePath = "/posters/" + fileName;

        savePosterAsPng(filePath, svgContent);

        return filePath;
    }

    /**
     * 使用默认模板生成海报
     */
    private String generateWithDefaultTemplate(Map<String, String> data) {
        String templateContent = getDefaultTemplate();
        Map<String, String> processedData = processDataWithTextWrapping(data);
        String svgContent = replacePlaceholders(templateContent, processedData);

        String fileName = "poster_" + System.currentTimeMillis() + ".png";
        String filePath = "/posters/" + fileName;

        savePosterAsPng(filePath, svgContent);

        return filePath;
    }

    /**
     * 读取模板文件
     */
    private String readTemplate(String templatePath) {
        try {
            Path path = Paths.get(basePath, templatePath);
            if (Files.exists(path)) {
                return Files.readString(path, StandardCharsets.UTF_8);
            }

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
     * 将文本按指定字符数分割成多行
     * @param text 原始文本
     * @param charsPerLine 每行字符数
     * @return 分割后的行列表
     */
    private List<String> splitTextToLines(String text, int charsPerLine) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            lines.add("");
            return lines;
        }

        // 先按原有换行符分割
        String[] paragraphs = text.split("\n");

        for (String paragraph : paragraphs) {
            if (paragraph.isEmpty()) {
                lines.add("");
                continue;
            }

            // 对每个段落按字符数分割
            StringBuilder currentLine = new StringBuilder();
            int charCount = 0;

            for (char c : paragraph.toCharArray()) {
                // 中文字符算1个，英文/数字/符号算0.5个
                double charWidth = (c >= 0x4E00 && c <= 0x9FFF) ? 1.0 : 0.5;
                charCount += (int) Math.ceil(charWidth);

                if (charCount > charsPerLine && currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                    charCount = (int) Math.ceil(charWidth);
                }
                currentLine.append(c);
            }

            if (currentLine.length() > 0) {
                lines.add(currentLine.toString());
            }
        }

        return lines;
    }

    /**
     * 将文本转换为SVG tspan多行格式
     * @param text 原始文本
     * @param x x坐标
     * @param startY 起始y坐标
     * @param lineHeight 行高
     * @param charsPerLine 每行字符数
     * @return SVG tspan格式字符串
     */
    private String formatTextToTspan(String text, int x, int startY, int lineHeight, int charsPerLine) {
        List<String> lines = splitTextToLines(text, charsPerLine);
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < lines.size(); i++) {
            String line = escapeXml(lines.get(i));
            if (i == 0) {
                sb.append(line);
            } else {
                sb.append("<tspan x=\"").append(x).append("\" dy=\"").append(lineHeight).append("\">")
                  .append(line).append("</tspan>");
            }
        }

        return sb.toString();
    }

    /**
     * 处理数据，对长文本进行换行处理
     */
    private Map<String, String> processDataWithTextWrapping(Map<String, String> data) {
        Map<String, String> result = new HashMap<>(data);

        // 横版模板参数：x=70, lineHeight=22, charsPerLine=60
        String responsibilities = data.get("responsibilities");
        String requirements = data.get("requirements");
        String welfare = data.get("welfare");

        result.put("responsibilities", formatTextToTspan(responsibilities, 70, 350, 22, 60));
        result.put("requirements", formatTextToTspan(requirements, 70, 535, 22, 60));
        result.put("welfare", formatTextToTspan(welfare, 70, 740, 22, 60));

        return result;
    }

    /**
     * 转义XML特殊字符
     */
    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }

    /**
     * 将SVG转换为PNG并保存
     */
    private void savePosterAsPng(String filePath, String svgContent) {
        try {
            // 创建输出目录
            Path path = Paths.get(basePath, filePath);
            Files.createDirectories(path.getParent());

            // 使用Batik将SVG转换为PNG
            PNGTranscoder transcoder = new PNGTranscoder();

            transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, POSTER_WIDTH);
            transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, POSTER_HEIGHT);

            // 创建输入源
            ByteArrayInputStream svgStream = new ByteArrayInputStream(
                svgContent.getBytes(StandardCharsets.UTF_8));
            TranscoderInput input = new TranscoderInput(svgStream);

            // 转换到内存
            ByteArrayOutputStream pngStream = new ByteArrayOutputStream();
            TranscoderOutput output = new TranscoderOutput(pngStream);

            transcoder.transcode(input, output);

            // 保存PNG文件
            Files.write(path, pngStream.toByteArray());

            log.info("海报生成成功: {} (PNG格式, {}x{})", filePath, (int)POSTER_WIDTH, (int)POSTER_HEIGHT);
        } catch (Exception e) {
            log.error("保存海报文件失败: {}", e.getMessage());
            throw new RuntimeException("保存海报文件失败: " + e.getMessage());
        }
    }

    /**
     * 获取默认模板（横版 - 简约蓝白风格 1920x1080）
     */
    private String getDefaultTemplate() {
        return """
            <svg width="1920" height="1080" viewBox="0 0 1920 1080" xmlns="http://www.w3.org/2000/svg">
              <defs>
                <linearGradient id="bgGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" style="stop-color:#1E3A5F"/>
                  <stop offset="100%" style="stop-color:#2B5B8A"/>
                </linearGradient>
              </defs>
              <rect width="1920" height="1080" fill="url(#bgGrad)"/>
              <g transform="translate(80, 80)">
                <circle cx="300" cy="350" r="280" fill="#3B82F6" opacity="0.15"/>
                <circle cx="300" cy="350" r="180" fill="#3B82F6" opacity="0.2"/>
                <text x="300" y="180" font-family="Noto Sans CJK SC, sans-serif" font-size="24" fill="#FFFFFF" text-anchor="middle" opacity="0.9">{{company}}</text>
                <text x="300" y="280" font-family="Noto Sans CJK SC, sans-serif" font-size="72" fill="#FFFFFF" text-anchor="middle" font-weight="bold">诚聘英才</text>
                <text x="300" y="340" font-family="Arial, sans-serif" font-size="28" fill="#FFFFFF" text-anchor="middle" opacity="0.8">JOIN US</text>
                <rect x="200" y="370" width="200" height="4" fill="#4A90C8" rx="2"/>
                <rect x="50" y="420" width="500" height="80" rx="10" fill="#FFFFFF" opacity="0.95"/>
                <text x="300" y="475" font-family="Noto Sans CJK SC, sans-serif" font-size="36" fill="#1E3A5F" text-anchor="middle" font-weight="bold">{{jobTitle}}</text>
                <text x="300" y="560" font-family="Noto Sans CJK SC, sans-serif" font-size="42" fill="#FFD700" text-anchor="middle" font-weight="bold">{{salary}}</text>
              </g>
              <g transform="translate(720, 80)">
                <rect x="0" y="0" width="1100" height="920" rx="20" fill="#FFFFFF"/>
                <text x="550" y="50" font-family="Arial, sans-serif" font-size="24" fill="#64748B" text-anchor="middle">POSITION DETAILS</text>
                <text x="550" y="90" font-family="Noto Sans CJK SC, sans-serif" font-size="32" fill="#1E3A5F" text-anchor="middle" font-weight="bold">职位详情</text>
                <line x1="50" y1="115" x2="1050" y2="115" stroke="#E2E8F0" stroke-width="2"/>
                <g transform="translate(50, 140)">
                  <rect x="0" y="0" width="480" height="50" rx="8" fill="#F8FAFC"/>
                  <text x="20" y="33" font-family="Noto Sans CJK SC, sans-serif" font-size="16" fill="#64748B">工作地点</text>
                  <text x="460" y="33" font-family="Noto Sans CJK SC, sans-serif" font-size="16" fill="#1E293B" text-anchor="end" font-weight="500">{{location}}</text>
                  <rect x="500" y="0" width="480" height="50" rx="8" fill="#F8FAFC"/>
                  <text x="520" y="33" font-family="Noto Sans CJK SC, sans-serif" font-size="16" fill="#64748B">学历要求</text>
                  <text x="960" y="33" font-family="Noto Sans CJK SC, sans-serif" font-size="16" fill="#1E293B" text-anchor="end" font-weight="500">{{education}}</text>
                  <rect x="0" y="65" width="480" height="50" rx="8" fill="#F8FAFC"/>
                  <text x="20" y="98" font-family="Noto Sans CJK SC, sans-serif" font-size="16" fill="#64748B">经验要求</text>
                  <text x="460" y="98" font-family="Noto Sans CJK SC, sans-serif" font-size="16" fill="#1E293B" text-anchor="end" font-weight="500">{{experience}}</text>
                  <rect x="500" y="65" width="480" height="50" rx="8" fill="#F8FAFC"/>
                  <text x="520" y="98" font-family="Noto Sans CJK SC, sans-serif" font-size="16" fill="#64748B">招聘人数</text>
                  <text x="960" y="98" font-family="Noto Sans CJK SC, sans-serif" font-size="16" fill="#1E293B" text-anchor="end" font-weight="500">{{recruitCount}}人</text>
                </g>
                <text x="50" y="300" font-family="Noto Sans CJK SC, sans-serif" font-size="20" fill="#1E3A5F" font-weight="bold">岗位职责</text>
                <rect x="50" y="320" width="1000" height="120" rx="10" fill="#F8FAFC" stroke="#E2E8F0" stroke-width="1"/>
                <text x="70" y="350" font-family="Noto Sans CJK SC, sans-serif" font-size="15" fill="#475569">{{responsibilities}}</text>
                <text x="50" y="480" font-family="Noto Sans CJK SC, sans-serif" font-size="20" fill="#1E3A5F" font-weight="bold">任职要求</text>
                <rect x="50" y="500" width="1000" height="140" rx="10" fill="#F8FAFC" stroke="#E2E8F0" stroke-width="1"/>
                <text x="70" y="535" font-family="Noto Sans CJK SC, sans-serif" font-size="15" fill="#475569">{{requirements}}</text>
                <text x="50" y="680" font-family="Noto Sans CJK SC, sans-serif" font-size="20" fill="#1E3A5F" font-weight="bold">福利待遇</text>
                <rect x="50" y="700" width="1000" height="100" rx="10" fill="#EFF6FF"/>
                <text x="70" y="740" font-family="Noto Sans CJK SC, sans-serif" font-size="15" fill="#475569">{{welfare}}</text>
                <rect x="50" y="830" width="1000" height="60" rx="10" fill="#1E3A5F"/>
                <text x="150" y="868" font-family="Noto Sans CJK SC, sans-serif" font-size="18" fill="#FFFFFF">联系人：{{contactName}}</text>
                <text x="550" y="868" font-family="Noto Sans CJK SC, sans-serif" font-size="18" fill="#FFFFFF" text-anchor="middle">联系电话：{{contactPhone}}</text>
                <text x="950" y="868" font-family="Noto Sans CJK SC, sans-serif" font-size="16" fill="#FFFFFF" text-anchor="end" opacity="0.8">期待您的加入</text>
              </g>
            </svg>
            """;
    }
}