package com.tv.recruitment.common.utils;

/**
 * 格式化工具类
 */
public class FormatUtils {

    private FormatUtils() {
        // 工具类不允许实例化
    }

    /**
     * 格式化薪资范围
     *
     * @param salaryMin 最低薪资
     * @param salaryMax 最高薪资
     * @return 格式化后的薪资字符串
     */
    public static String formatSalary(Integer salaryMin, Integer salaryMax) {
        if (salaryMin == null && salaryMax == null) {
            return "面议";
        }
        if (salaryMin != null && salaryMax != null) {
            return salaryMin + "-" + salaryMax + "元";
        }
        if (salaryMin != null) {
            return salaryMin + "元以上";
        }
        return "面议";
    }

    /**
     * 格式化文件大小
     *
     * @param size 文件大小（字节）
     * @return 格式化后的文件大小字符串
     */
    public static String formatFileSize(Long size) {
        if (size == null || size < 0) {
            return "0 B";
        }
        if (size < 1024) {
            return size + " B";
        }
        double kb = size / 1024.0;
        if (kb < 1024) {
            return String.format("%.2f KB", kb);
        }
        double mb = kb / 1024.0;
        if (mb < 1024) {
            return String.format("%.2f MB", mb);
        }
        double gb = mb / 1024.0;
        return String.format("%.2f GB", gb);
    }

    /**
     * 格式化时长（秒转为时:分:秒）
     *
     * @param seconds 秒数
     * @return 格式化后的时长字符串
     */
    public static String formatDuration(Integer seconds) {
        if (seconds == null || seconds < 0) {
            return "00:00";
        }
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        }
        return String.format("%02d:%02d", minutes, secs);
    }
}