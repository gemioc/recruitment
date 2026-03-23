package com.tv.recruitment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tv.recruitment.entity.PushRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 推送记录Mapper
 */
@Mapper
public interface PushRecordMapper extends BaseMapper<PushRecord> {
}