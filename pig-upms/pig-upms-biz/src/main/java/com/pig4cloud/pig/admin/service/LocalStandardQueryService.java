package com.pig4cloud.pig.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pig4cloud.pig.admin.dto.FilterOptionsDTO;
import com.pig4cloud.pig.admin.dto.LocalStandardDetailResponseDTO;
import com.pig4cloud.pig.admin.dto.LocalStandardQueryDTO;
import com.pig4cloud.pig.admin.dto.LocalStandardQueryResponseDTO;
import com.pig4cloud.pig.admin.entity.LocalStandardDetail;

import java.util.List;

/**
 * 地方标准查询Service
 *
 * @author pig
 * @date 2025-10-21
 */
public interface LocalStandardQueryService extends IService<LocalStandardDetail> {

    /**
     * 根据省份名称查询标准列表
     * @param provinceName 省份名称，如"辽宁"、"北京"等
     * @return 标准列表
     */
    List<LocalStandardDetail> getStandardsByProvince(String provinceName);

    /**
     * 根据城市分类ID查询标准列表
     * @param cityCategoryId 城市分类ID
     * @return 标准列表
     */
    List<LocalStandardDetail> getStandardsByCityCategoryId(Long cityCategoryId);

    /**
     * 统计指定省份的标准数量
     * @param provinceName 省份名称
     * @return 标准数量
     */
    long countStandardsByProvince(String provinceName);

    /**
     * 获取省份标准统计信息
     * @param provinceName 省份名称
     * @return 统计信息
     */
    ProvinceStandardStats getProvinceStandardStats(String provinceName);

    /**
     * 分页查询地方标准
     * @param page 分页参数
     * @param queryDTO 查询条件
     * @return 分页查询结果
     */
    LocalStandardQueryResponseDTO queryLocalStandards(Page<LocalStandardDetail> page, LocalStandardQueryDTO queryDTO);

    /**
     * 获取筛选条件选项
     * @return 筛选条件选项
     */
    FilterOptionsDTO getFilterOptions();

    /**
     * 根据pk或code获取标准详情
     * @param pk 标准唯一标识（优先使用）
     * @param code 标准号（pk为空时使用）
     * @return 标准详情
     */
    LocalStandardDetailResponseDTO getStandardDetail(String pk, String code);

    /**
     * 省份标准统计信息
     */
    class ProvinceStandardStats {
        private String provinceName;
        private long totalCount;
        private long activeCount;
        private long abolishedCount;
        
        // getters and setters
        public String getProvinceName() { return provinceName; }
        public void setProvinceName(String provinceName) { this.provinceName = provinceName; }
        public long getTotalCount() { return totalCount; }
        public void setTotalCount(long totalCount) { this.totalCount = totalCount; }
        public long getActiveCount() { return activeCount; }
        public void setActiveCount(long activeCount) { this.activeCount = activeCount; }
        public long getAbolishedCount() { return abolishedCount; }
        public void setAbolishedCount(long abolishedCount) { this.abolishedCount = abolishedCount; }
    }
}
