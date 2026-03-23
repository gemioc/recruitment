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

import java.util.List;

/**
 * 海报控制器
 */
@Tag(name = "海报管理")
@RestController
@RequestMapping("/api/posters")
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

    @Operation(summary = "生成海报")
    @PostMapping
    public Result<Poster> generate(@RequestBody Poster poster) {
        // TODO: 实际的海报生成逻辑
        posterMapper.insert(poster);
        return Result.success(poster);
    }

    @Operation(summary = "删除海报")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        posterMapper.deleteById(id);
        return Result.success();
    }
}