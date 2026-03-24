package com.tv.recruitment.common.utils;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * CSV导出工具类
 */
public class CsvExportUtils {

    private CsvExportUtils() {
        // 工具类不允许实例化
    }

    /**
     * UTF-8 BOM字节，用于Excel正确识别UTF-8编码
     */
    public static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    /**
     * 设置CSV导出响应头
     *
     * @param response HTTP响应
     * @param fileName 文件名（不含扩展名）
     */
    public static void setCsvResponseHeaders(HttpServletResponse response, String fileName) {
        String encodedFileName = java.net.URLEncoder.encode(fileName + ".csv", StandardCharsets.UTF_8);
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=\"" + encodedFileName + "\"");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
    }

    /**
     * 写入UTF-8 BOM
     *
     * @param outputStream 输出流
     * @throws IOException IO异常
     */
    public static void writeBom(OutputStream outputStream) throws IOException {
        outputStream.write(UTF8_BOM);
    }

    /**
     * CSV字段转义
     * 如果字段包含逗号、双引号或换行符，需要用双引号包围
     * 字段中的双引号需要转义为两个双引号
     *
     * @param value 原始值
     * @return 转义后的值
     */
    public static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        // 如果包含特殊字符，需要用双引号包围并转义内部双引号
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * 构建CSV行
     *
     * @param fields 字段数组
     * @return CSV行字符串
     */
    public static String buildCsvLine(String... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(escapeCsv(fields[i]));
        }
        sb.append("\n");
        return sb.toString();
    }
}