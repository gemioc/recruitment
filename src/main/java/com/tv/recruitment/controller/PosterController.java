package com.tv.recruitment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tv.recruitment.common.result.Result;
import com.tv.recruitment.entity.Poster;
import com.tv.recruitment.entity.PosterTemplate;
import com.tv.recruitment.mapper.PosterMapper;
import com.tv.recruitment.mapper.PosterTemplateMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public Result<Page<Poster>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Poster> page = new Page<>(pageNum, pageSize);
        Page<Poster> result = posterMapper.selectPage(page,
                new LambdaQueryWrapper<Poster>().orderByDesc(Poster::getCreateTime));
        return Result.success(result);
    }

    @Operation(summary = "获取海报详情")
    @GetMapping("/{id}")
    public Result<Poster> getById(@PathVariable Long id) {
        return Result.success(posterMapper.selectById(id));
    }

    @Operation(summary = "生成海报")
    @PostMapping
    public Result<Poster> generate(@RequestBody Poster poster) {
        // TODO: 实际的海报生成逻辑
        posterMapper.insert(poster);
        return Result.success(poster);
    }

    @Operation(summary = "预览海报")
    @PostMapping("/preview")
    public Result<String> preview(@RequestBody Map<String, Object> data) {
        // TODO: 实际的海报预览生成逻辑
        Long jobId = Long.valueOf(data.get("jobId").toString());
        Long templateId = data.get("templateId") != null ? Long.valueOf(data.get("templateId").toString()) : null;
        return Result.success("/files/posters/preview.png");
    }

    @Operation(summary = "生成海报(别名)")
    @PostMapping("/generate")
    public Result<Poster> generateAlias(@RequestBody Poster poster) {
        // TODO: 实际的海报生成逻辑
        posterMapper.insert(poster);
        return Result.success(poster);
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
            poster.setFilePath("/files/posters/poster_" + jobId + ".png");
            posterMapper.insert(poster);
            posters.add(poster);
        }
        return Result.success(posters);
    }

    @Operation(summary = "导出海报")
    @GetMapping("/{id}/export")
    public void export(@PathVariable Long id, @RequestParam(defaultValue = "jpg") String format,
                       jakarta.servlet.http.HttpServletResponse response) {
        // TODO: 实际的导出逻辑
        Poster poster = posterMapper.selectById(id);
        if (poster != null) {
            try {
                response.setContentType("application/octet-stream");
                response.setHeader("Content-Disposition", "attachment;filename=poster_" + id + "." + format);
                response.getOutputStream().close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    @Operation(summary = "批量导出海报")
    @PostMapping("/export")
    public void batchExport(@RequestBody Map<String, Object> data,
                            jakarta.servlet.http.HttpServletResponse response) {
        // TODO: 实际的批量导出逻辑
        List<Long> ids = (List<Long>) data.get("ids");
        String format = (String) data.getOrDefault("format", "jpg");
        try {
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment;filename=posters.zip");
            response.getOutputStream().close();
        } catch (Exception e) {
            // ignore
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
}