package com.tv.recruitment.common.utils;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 文件下载工具类
 */
public class FileDownloadUtils {

    private FileDownloadUtils() {
        // 工具类不允许实例化
    }

    /**
     * 设置文件下载响应头
     *
     * @param response HTTP响应
     * @param fileName 文件名
     * @param contentType 内容类型
     */
    public static void setDownloadHeaders(HttpServletResponse response, String fileName, String contentType) {
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        response.setContentType(contentType);
        response.setHeader("Content-Disposition", "attachment;filename=\"" + encodedFileName + "\"");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
    }

    /**
     * 设置文件下载响应头（默认二进制流）
     *
     * @param response HTTP响应
     * @param fileName 文件名
     */
    public static void setDownloadHeaders(HttpServletResponse response, String fileName) {
        setDownloadHeaders(response, fileName, MediaType.APPLICATION_OCTET_STREAM_VALUE);
    }

    /**
     * 设置CSV文件下载响应头
     *
     * @param response HTTP响应
     * @param fileName 文件名（不含扩展名）
     */
    public static void setCsvDownloadHeaders(HttpServletResponse response, String fileName) {
        CsvExportUtils.setCsvResponseHeaders(response, fileName);
    }

    /**
     * 设置ZIP文件下载响应头
     *
     * @param response HTTP响应
     * @param fileName 文件名（不含扩展名）
     */
    public static void setZipDownloadHeaders(HttpServletResponse response, String fileName) {
        String encodedFileName = URLEncoder.encode(fileName + ".zip", StandardCharsets.UTF_8);
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment;filename=\"" + encodedFileName + "\"");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
    }

    /**
     * 写入错误响应
     *
     * @param response HTTP响应
     * @param code 错误码
     * @param message 错误消息
     */
    public static void writeErrorResponse(HttpServletResponse response, int code, String message) {
        try {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":" + code + ",\"message\":\"" + escapeJson(message) + "\"}");
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * 简单的JSON字符串转义
     */
    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}