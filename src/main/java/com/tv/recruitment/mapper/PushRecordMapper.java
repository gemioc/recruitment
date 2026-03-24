package com.tv.recruitment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tv.recruitment.entity.PushRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 推送记录Mapper
 */
@Mapper
public interface PushRecordMapper extends BaseMapper<PushRecord> {

    /**
     * 聚合统计：一次查询获取总数、成功数、失败数、海报数、视频数
     */
    @Select("""
        SELECT
            COUNT(*) as total,
            SUM(CASE WHEN push_status = 1 THEN 1 ELSE 0 END) as success,
            SUM(CASE WHEN push_status = 2 THEN 1 ELSE 0 END) as fail,
            SUM(CASE WHEN content_type = 1 THEN 1 ELSE 0 END) as posterCount,
            SUM(CASE WHEN content_type = 2 THEN 1 ELSE 0 END) as videoCount
        FROM t_push_record
        WHERE push_time >= #{start} AND push_time <= #{end}
        """)
    Map<String, Object> selectPushSummary(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * 按日期分组查询趋势
     */
    @Select("""
        SELECT
            DATE(push_time) as date,
            COUNT(*) as total,
            SUM(CASE WHEN push_status = 1 THEN 1 ELSE 0 END) as success,
            SUM(CASE WHEN content_type = 1 THEN 1 ELSE 0 END) as posterCount,
            SUM(CASE WHEN content_type = 2 THEN 1 ELSE 0 END) as videoCount
        FROM t_push_record
        WHERE push_time >= #{start} AND push_time <= #{end}
        GROUP BY DATE(push_time)
        ORDER BY DATE(push_time)
        """)
    List<Map<String, Object>> selectDailyTrend(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * 按小时分组查询时段分布
     */
    @Select("""
        SELECT
            HOUR(push_time) as hour,
            COUNT(*) as count
        FROM t_push_record
        WHERE push_time >= #{start} AND push_time <= #{end}
        GROUP BY HOUR(push_time)
        ORDER BY HOUR(push_time)
        """)
    List<Map<String, Object>> selectHourDistribution(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * 按设备分组查询推送排行
     * 需要解析JSON数组中的设备ID
     */
    @Select("""
        SELECT
            SUBSTRING_INDEX(SUBSTRING_INDEX(pr.target_ids, ',', n.n), ',', -1) as device_id,
            COUNT(*) as push_count
        FROM t_push_record pr
        CROSS JOIN (
            SELECT 1 as n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
            UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
            UNION SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15
            UNION SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20
            UNION SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 UNION SELECT 25
            UNION SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29 UNION SELECT 30
            UNION SELECT 31 UNION SELECT 32 UNION SELECT 33 UNION SELECT 34 UNION SELECT 35
            UNION SELECT 36 UNION SELECT 37 UNION SELECT 38 UNION SELECT 39 UNION SELECT 40
            UNION SELECT 41 UNION SELECT 42 UNION SELECT 43 UNION SELECT 44 UNION SELECT 45
            UNION SELECT 46 UNION SELECT 47 UNION SELECT 48 UNION SELECT 49 UNION SELECT 50
        ) n
        WHERE pr.push_time >= #{start}
            AND pr.push_time <= #{end}
            AND n.n <= LENGTH(pr.target_ids) - LENGTH(REPLACE(pr.target_ids, ',', '')) + 1
        GROUP BY device_id
        ORDER BY push_count DESC
        LIMIT #{limit}
        """)
    List<Map<String, Object>> selectDeviceRank(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("limit") int limit);
}