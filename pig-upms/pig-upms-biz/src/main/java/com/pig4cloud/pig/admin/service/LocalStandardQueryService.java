package com.pig4cloud.pig.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
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
