package com.tv.recruitment.service.impl;

import com.tv.recruitment.entity.Poster;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 海报生成服务实现
 * 生成 PNG 格式海报，兼容 Android Glide 加载
 * 前端负责 SVG 渲染，后端仅负责 SVG 转 PNG
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PosterGenerateServiceImpl implements PosterGenerateService {

    private final FileStorageService fileStorageService;

    @Value("${file.storage.base-path:D:/B-code_space/tv-files}")
    private String basePath;

    // 横版尺寸 - 1920x1080 Full HD
    private static final float POSTER_WIDTH = 1920f;
    private static final float POSTER_HEIGHT = 1080f;

    @Override
    public String generatePoster(Long templateId, java.util.Map<String, String> data) {
        // 此方法已废弃，前端不再调用
        throw new UnsupportedOperationException("此方法已废弃，请使用 generateFromSvg");
    }

    @Override
    public String generateFromJob(Poster poster) {
        // 此方法已废弃，前端不再调用
        throw new UnsupportedOperationException("此方法已废弃，请使用 generateFromSvg");
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

            // 创建输入源 - 传入空的基础URI，让Batik允许所有外部资源
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
            // 收集完整异常信息，避免嵌套异常丢失
            StringBuilder sb = new StringBuilder();
            sb.append("保存海报文件失败: ").append(e.getMessage()).append("\n");

            Throwable cause = e.getCause();
            int level = 1;
            while (cause != null && level <= 5) {
                sb.append("  原因").append(level).append(": ").append(cause.getMessage());
                if (cause.getCause() != null && cause.getCause() != cause) {
                    sb.append(" <- ");
                } else {
                    sb.append("\n");
                }
                cause = cause.getCause();
                level++;
            }

            // 输出完整堆栈到日志
            log.error("{}", sb.toString());
            log.error("完整异常堆栈: ", e);

            throw new RuntimeException("海报生成失败: " + e.getClass().getSimpleName()
                + (e.getCause() != null ? " - " + e.getCause().getMessage() : ""));
        }
    }
}
