package com.pig4cloud.pig.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pig4cloud.pig.admin.entity.LocalStandardDetailInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 地方标准详细信息Mapper
 *
 * @author pig
 * @date 2025-10-15
 */
@Mapper
public interface LocalStandardDetailInfoMapper extends BaseMapper<LocalStandardDetailInfo> {

    /**
     * 批量查询 pk 对应的 id（用于增量爬取时判断记录是否存在）
     * @param pks pk 列表
     * @return pk 和 id 的映射关系列表
     */
    @Select({
        "<script>",
        "SELECT pk, id FROM local_standard_detail_info ",
        "WHERE pk IN ",
        "<foreach collection='pks' item='pk' open='(' separator=',' close=')'>",
        "#{pk}",
        "</foreach>",
        "</script>"
    })
    List<Map<String, Object>> getPkToIdMapping(@Param("pks") List<String> pks);

}
