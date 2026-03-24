package com.tv.recruitment.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 海报列表响应
 */
@Data
public class PosterResponse {

    private Long id;

    private String posterName;

    private Long jobId;

    private String jobName;

    private Long templateId;

    private String templateName;

    private String filePath;

    private Long fileSize;

    private LocalDateTime createTime;
}