package com.pig4cloud.pig.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pig4cloud.pig.admin.entity.LocalStandardDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 地方标准详情Mapper
 *
 * @author pig
 * @date 2025-10-15
 */
@Mapper
public interface LocalStandardDetailMapper extends BaseMapper<LocalStandardDetail> {

    /**
     * 获取需要爬取详细信息的标准PK列表（分页）
     * 只返回还没有详细信息记录的标准
     */
    @Select("SELECT d.pk FROM local_standard_detail d " +
            "LEFT JOIN local_standard_detail_info di ON d.pk = di.pk " +
            "WHERE di.pk IS NULL " +
            "ORDER BY d.create_time " +
            "LIMIT #{offset}, #{limit}")
    List<String> getPksNeedingDetailInfo(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 获取需要爬取详细信息的标准总数
     */
    @Select("SELECT COUNT(*) FROM local_standard_detail d " +
            "LEFT JOIN local_standard_detail_info di ON d.pk = di.pk " +
            "WHERE di.pk IS NULL")
    long getCountNeedingDetailInfo();

    /**
     * 获取需要下载文档的标准PK列表（分页）
     * 只返回还没有下载记录的标准
     */
    @Select("SELECT d.pk FROM local_standard_detail d " +
            "LEFT JOIN local_standard_document doc ON d.pk = doc.pk " +
            "WHERE doc.pk IS NULL " +
            "ORDER BY d.create_time " +
            "LIMIT #{offset}, #{limit}")
    List<String> getPksNeedingDownload(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 获取需要下载文档的标准总数
     */
    @Select("SELECT COUNT(*) FROM local_standard_detail d " +
            "LEFT JOIN local_standard_document doc ON d.pk = doc.pk " +
            "WHERE doc.pk IS NULL")
    long getCountNeedingDownload();

    /**
     * 获取需要重试下载的标准PK列表（分页）
     * 返回下载失败且重试次数小于最大重试次数的标准
     */
    @Select("SELECT d.pk FROM local_standard_detail d " +
            "INNER JOIN local_standard_document doc ON d.pk = doc.pk " +
            "WHERE doc.download_status = 'FAILED' " +
            "AND doc.retry_count < #{maxRetries} " +
            "ORDER BY doc.update_time " +
            "LIMIT #{offset}, #{limit}")
    List<String> getPksNeedingRetry(@Param("offset") int offset, @Param("limit") int limit, @Param("maxRetries") int maxRetries);

    /**
     * 获取需要重试下载的标准总数
     */
    @Select("SELECT COUNT(*) FROM local_standard_detail d " +
            "INNER JOIN local_standard_document doc ON d.pk = doc.pk " +
            "WHERE doc.download_status = 'FAILED' " +
            "AND doc.retry_count < #{maxRetries}")
    long getCountNeedingRetry(@Param("maxRetries") int maxRetries);

    /**
     * 根据省份名称查询标准列表（通过city_code关联）
     */
    @Select("SELECT d.*, c.city_name, c.standard_count " +
            "FROM local_standard_detail d " +
            "INNER JOIN local_standard_city_category c ON d.city_code = c.city_code " +
            "WHERE c.city_name LIKE CONCAT('%', #{provinceName}, '%') " +
            "ORDER BY d.create_time DESC")
    List<Map<String, Object>> getStandardsByProvinceWithCityInfo(@Param("provinceName") String provinceName);

    /**
     * 根据城市代码查询标准列表
     */
    @Select("SELECT * FROM local_standard_detail " +
            "WHERE city_code = #{cityCode} " +
            "ORDER BY create_time DESC")
    List<LocalStandardDetail> getStandardsByCityCode(@Param("cityCode") String cityCode);
}
