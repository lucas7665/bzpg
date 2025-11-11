package com.pig4cloud.pig.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pig4cloud.pig.admin.dto.IndustryStandardDetailResponseDTO;
import com.pig4cloud.pig.admin.dto.IndustryStandardFilterOptionsDTO;
import com.pig4cloud.pig.admin.dto.IndustryStandardQueryDTO;
import com.pig4cloud.pig.admin.dto.IndustryStandardQueryResponseDTO;
import com.pig4cloud.pig.admin.entity.IndustryStandardDetail;

/**
 * 行业标准查询Service
 *
 * @author pig
 * @date 2025-01-XX
 */
public interface IndustryStandardQueryService extends IService<IndustryStandardDetail> {

    /**
     * 分页查询行业标准
     * @param page 分页参数
     * @param queryDTO 查询条件
     * @return 分页查询结果
     */
    IndustryStandardQueryResponseDTO queryIndustryStandards(Page<IndustryStandardDetail> page, IndustryStandardQueryDTO queryDTO);

    /**
     * 获取筛选条件选项
     * @return 筛选条件选项
     */
    IndustryStandardFilterOptionsDTO getFilterOptions();

    /**
     * 根据pk或code获取标准详情
     * @param pk 标准唯一标识（优先使用）
     * @param code 标准号（pk为空时使用）
     * @return 标准详情
     */
    IndustryStandardDetailResponseDTO getStandardDetail(String pk, String code);

}

