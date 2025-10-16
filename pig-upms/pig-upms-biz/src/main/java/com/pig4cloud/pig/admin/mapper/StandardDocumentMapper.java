package com.pig4cloud.pig.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pig4cloud.pig.admin.entity.StandardDocument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 标准文档下载记录Mapper
 *
 * @author pig
 * @date 2025-10-15
 */
@Mapper
public interface StandardDocumentMapper extends BaseMapper<StandardDocument> {

    /**
     * 获取需要下载的标准PK列表（分页）
     * 只返回还没有下载记录的标准
     */
    @Select("SELECT d.pk FROM industry_standard_detail d " +
            "LEFT JOIN standard_document sd ON d.pk = sd.pk " +
            "WHERE sd.pk IS NULL " +
            "ORDER BY d.create_time " +
            "LIMIT #{offset}, #{limit}")
    List<String> getPksNeedingDownload(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 获取需要下载的标准总数
     */
    @Select("SELECT COUNT(*) FROM industry_standard_detail d " +
            "LEFT JOIN standard_document sd ON d.pk = sd.pk " +
            "WHERE sd.pk IS NULL")
    long getCountNeedingDownload();

    /**
     * 获取需要重试下载的标准PK列表（分页）
     * 返回下载失败且重试次数小于最大重试次数的标准
     */
    @Select("SELECT pk FROM standard_document " +
            "WHERE download_status = 'FAILED' " +
            "AND retry_count < #{maxRetries} " +
            "ORDER BY update_time " +
            "LIMIT #{offset}, #{limit}")
    List<String> getPksNeedingRetry(@Param("offset") int offset, @Param("limit") int limit, @Param("maxRetries") int maxRetries);

    /**
     * 获取需要重试下载的标准总数
     */
    @Select("SELECT COUNT(*) FROM standard_document " +
            "WHERE download_status = 'FAILED' " +
            "AND retry_count < #{maxRetries}")
    long getCountNeedingRetry(@Param("maxRetries") int maxRetries);
}
