package com.tv.recruitment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.tv.recruitment.common.annotation.Log;
import com.tv.recruitment.common.result.Result;
import com.tv.recruitment.dto.response.PosterResponse;
import com.tv.recruitment.entity.Poster;
import com.tv.recruitment.entity.PosterTemplate;
import com.tv.recruitment.service.PosterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 海报控制器
 *
 * @author tv_recru
 */
@Tag(name = "海报管理")
@RestController
@RequestMapping("/posters")
@RequiredArgsConstructor
public class PosterController {

    private final PosterService posterService;

    @Operation(summary = "获取海报模板列表")
    @GetMapping("/templates")
    public Result<List<PosterTemplate>> getTemplates() {
        return Result.success(posterService.getTemplates());
    }

    @Operation(summary = "分页查询海报")
    @GetMapping
    public Result<Page<PosterResponse>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String posterName) {
        return Result.success(posterService.page(pageNum, pageSize, posterName));
    }

    @Operation(summary = "获取海报详情")
    @GetMapping("/{id}")
    public Result<PosterResponse> getById(@PathVariable Long id) {
        return Result.success(posterService.getById(id));
    }

    @Operation(summary = "生成海报")
    @PostMapping
    @Log(type = "CREATE", desc = "生成海报")
    public Result<Poster> generate(@RequestBody Poster poster) {
        return Result.success(posterService.generate(poster));
    }

    @Operation(summary = "预览海报")
    @PostMapping("/preview")
    public Result<String> preview(@RequestBody Map<String, Object> data) {
        return Result.success(posterService.preview(data));
    }

    @Operation(summary = "生成海报(别名)")
    @PostMapping("/generate")
    @Log(type = "CREATE", desc = "生成海报")
    public Result<PosterResponse> generateAlias(@RequestBody Poster poster) {
        return Result.success(posterService.generateAlias(poster));
    }

    @Operation(summary = "批量生成海报")
    @PostMapping("/batch")
    @Log(type = "CREATE", desc = "批量生成海报")
    public Result<List<Poster>> batchGenerate(@RequestBody Map<String, Object> data) throws JsonProcessingException {
        List<Long> jobIds = (List<Long>) data.get("jobIds");
        String svgContent = (String) data.get("svgContent");
        String posterName = (String) data.get("posterName");
        // 解析 templateId（可能是 Long 或 String "multi_01"）
        Long templateId = null;
        Object tid = data.get("templateId");
        if (tid != null) {
            try {
                templateId = Long.valueOf(tid.toString());
            } catch (NumberFormatException e) {
                // 如果是字符串（如 "multi_01"），不转换为 Long，但仍然存储用于显示
            }
        }
        return Result.success(posterService.batchGenerate(jobIds, templateId, svgContent, posterName));
    }

    @Operation(summary = "导出海报")
    @GetMapping("/{id}/export")
    public void export(@PathVariable Long id,
                      @RequestParam(defaultValue = "png") String format,
                      HttpServletResponse response) {
        posterService.export(id, format, response);
    }

    @Operation(summary = "批量导出海报")
    @PostMapping("/export")
    public void batchExport(@RequestBody Map<String, Object> data,
                           HttpServletResponse response) {
        List<Long> ids = (List<Long>) data.get("ids");
        String format = (String) data.getOrDefault("format", "png");
        posterService.batchExport(ids, format, response);
    }

    @Operation(summary = "编辑海报")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Poster poster) {
        posterService.update(id, poster);
        return Result.success();
    }

    @Operation(summary = "删除海报")
    @DeleteMapping("/{id}")
    @Log(type = "DELETE", desc = "删除海报")
    public Result<Void> delete(@PathVariable Long id) {
        posterService.delete(id);
        return Result.success();
    }
}
