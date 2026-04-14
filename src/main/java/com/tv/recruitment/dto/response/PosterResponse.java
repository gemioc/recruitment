package com.tv.recruitment.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 海报列表响应
 */
@Data
public class PosterResponse {

    private Long id;

    private String posterName;

    private Long jobId;

    private String jobIds;

    private String jobName;

    private String relatedJobNames;

    private Long templateId;

    private String templateName;

    private String filePath;

    private Long fileSize;

    private LocalDateTime createTime;
}