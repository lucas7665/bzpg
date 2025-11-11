package com.pig4cloud.pig.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.admin.dto.FilterOptionsDTO;
import com.pig4cloud.pig.admin.dto.LocalStandardQueryDTO;
import com.pig4cloud.pig.admin.dto.LocalStandardQueryResponseDTO;
import com.pig4cloud.pig.admin.dto.LocalStandardRecordDTO;
import com.pig4cloud.pig.admin.entity.LocalStandardCityCategory;
import com.pig4cloud.pig.admin.entity.LocalStandardDetail;
import com.pig4cloud.pig.admin.mapper.LocalStandardDetailMapper;
import com.pig4cloud.pig.admin.service.LocalStandardCityCategoryService;
import com.pig4cloud.pig.admin.service.LocalStandardQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 地方标准查询Service实现
 *
 * @author pig
 * @date 2025-10-21
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocalStandardQueryServiceImpl extends ServiceImpl<LocalStandardDetailMapper, LocalStandardDetail> 
        implements LocalStandardQueryService {

    private final LocalStandardCityCategoryService cityCategoryService;

    @Override
    public List<LocalStandardDetail> getStandardsByProvince(String provinceName) {
        log.info("查询省份 {} 的标准列表", provinceName);
        
        // 方法1：通过city字段查询
        LambdaQueryWrapper<LocalStandardDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(LocalStandardDetail::getCity, provinceName)
               .orderByDesc(LocalStandardDetail::getCreateTime);
        
        List<LocalStandardDetail> result = list(wrapper);
        log.info("省份 {} 找到 {} 条标准", provinceName, result.size());
        
        return result;
    }

    /**
     * 根据城市代码查询标准列表
     * @param cityCode 城市代码，如bjzjj
     * @return 标准列表
     */
    public List<LocalStandardDetail> getStandardsByCityCode(String cityCode) {
        log.info("查询城市代码 {} 的标准列表", cityCode);
        
        LambdaQueryWrapper<LocalStandardDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LocalStandardDetail::getCityCode, cityCode)
               .orderByDesc(LocalStandardDetail::getCreateTime);
        
        List<LocalStandardDetail> result = list(wrapper);
        log.info("城市代码 {} 找到 {} 条标准", cityCode, result.size());
        
        return result;
    }

    @Override
    public List<LocalStandardDetail> getStandardsByCityCategoryId(Long cityCategoryId) {
        log.info("查询城市分类ID {} 的标准列表", cityCategoryId);
        
        LambdaQueryWrapper<LocalStandardDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LocalStandardDetail::getCityCategoryId, cityCategoryId)
               .orderByDesc(LocalStandardDetail::getCreateTime);
        
        List<LocalStandardDetail> result = list(wrapper);
        log.info("城市分类ID {} 找到 {} 条标准", cityCategoryId, result.size());
        
        return result;
    }

    @Override
    public long countStandardsByProvince(String provinceName) {
        log.info("统计省份 {} 的标准数量", provinceName);
        
        LambdaQueryWrapper<LocalStandardDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(LocalStandardDetail::getCity, provinceName);
        
        long count = count(wrapper);
        log.info("省份 {} 共有 {} 条标准", provinceName, count);
        
        return count;
    }

    @Override
    public ProvinceStandardStats getProvinceStandardStats(String provinceName) {
        log.info("获取省份 {} 的标准统计信息", provinceName);
        
        ProvinceStandardStats stats = new ProvinceStandardStats();
        stats.setProvinceName(provinceName);
        
        // 总数量
        LambdaQueryWrapper<LocalStandardDetail> totalWrapper = new LambdaQueryWrapper<>();
        totalWrapper.like(LocalStandardDetail::getCity, provinceName);
        stats.setTotalCount(count(totalWrapper));
        
        // 现行标准数量
        LambdaQueryWrapper<LocalStandardDetail> activeWrapper = new LambdaQueryWrapper<>();
        activeWrapper.like(LocalStandardDetail::getCity, provinceName)
                    .eq(LocalStandardDetail::getStatus, "现行");
        stats.setActiveCount(count(activeWrapper));
        
        // 废止标准数量
        LambdaQueryWrapper<LocalStandardDetail> abolishedWrapper = new LambdaQueryWrapper<>();
        abolishedWrapper.like(LocalStandardDetail::getCity, provinceName)
                       .eq(LocalStandardDetail::getStatus, "废止");
        stats.setAbolishedCount(count(abolishedWrapper));
        
        log.info("省份 {} 统计结果：总数={}, 现行={}, 废止={}", 
                provinceName, stats.getTotalCount(), stats.getActiveCount(), stats.getAbolishedCount());
        
        return stats;
    }

    @Override
    public LocalStandardQueryResponseDTO queryLocalStandards(Page<LocalStandardDetail> page, LocalStandardQueryDTO queryDTO) {
        log.info("分页查询地方标准，查询条件：{}", queryDTO);
        
        LambdaQueryWrapper<LocalStandardDetail> wrapper = new LambdaQueryWrapper<>();
        
        // 关键词查询（标准号或标准名称）
        if (StrUtil.isNotBlank(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like(LocalStandardDetail::getCode, queryDTO.getKeyword())
                    .or()
                    .like(LocalStandardDetail::getChName, queryDTO.getKeyword()));
        }
        
        // 城市代码筛选
        if (StrUtil.isNotBlank(queryDTO.getCityCode())) {
            wrapper.eq(LocalStandardDetail::getCityCode, queryDTO.getCityCode());
        }
        
        // 备案日期筛选
        if (StrUtil.isNotBlank(queryDTO.getRecordDateType())) {
            Long startTimestamp = calculateRecordDateStartTimestamp(queryDTO.getRecordDateType());
            if (startTimestamp != null) {
                wrapper.ge(LocalStandardDetail::getRecordDate, startTimestamp);
            }
        }
        
        // 状态筛选
        if (StrUtil.isNotBlank(queryDTO.getStatus())) {
            wrapper.eq(LocalStandardDetail::getStatus, queryDTO.getStatus());
        }
        
        // 按创建时间倒序排列
        wrapper.orderByDesc(LocalStandardDetail::getCreateTime);
        
        // 执行分页查询
        Page<LocalStandardDetail> resultPage = page(page, wrapper);
        
        // 转换为响应DTO
        LocalStandardQueryResponseDTO responseDTO = new LocalStandardQueryResponseDTO();
        responseDTO.setTotal(resultPage.getTotal());
        responseDTO.setSize(resultPage.getSize());
        responseDTO.setCurrent(resultPage.getCurrent());
        responseDTO.setPages(resultPage.getPages());
        
        // 转换记录列表
        List<LocalStandardRecordDTO> records = resultPage.getRecords().stream()
                .map(this::convertToRecordDTO)
                .collect(Collectors.toList());
        responseDTO.setRecords(records);
        
        log.info("查询完成，共找到 {} 条记录，当前页：{}，每页：{}", 
                resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize());
        
        return responseDTO;
    }

    @Override
    public FilterOptionsDTO getFilterOptions() {
        log.info("获取筛选条件选项");
        
        FilterOptionsDTO filterOptions = new FilterOptionsDTO();
        
        // 获取省份列表（带数量统计）
        List<LocalStandardCityCategory> cityCategories = cityCategoryService.list();
        List<FilterOptionsDTO.ProvinceOptionDTO> provinces = cityCategories.stream()
                .map(category -> {
                    FilterOptionsDTO.ProvinceOptionDTO province = new FilterOptionsDTO.ProvinceOptionDTO();
                    province.setCityCode(category.getCityCode());
                    province.setCityName(category.getCityName());
                    // 实时统计该省份的标准数量
                    LambdaQueryWrapper<LocalStandardDetail> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(LocalStandardDetail::getCityCode, category.getCityCode());
                    long count = count(wrapper);
                    province.setStandardCount((int) count);
                    return province;
                })
                .collect(Collectors.toList());
        filterOptions.setProvinces(provinces);
        
        // 状态选项
        List<FilterOptionsDTO.StatusOptionDTO> statusOptions = new ArrayList<>();
        FilterOptionsDTO.StatusOptionDTO status1 = new FilterOptionsDTO.StatusOptionDTO();
        status1.setValue("现行");
        status1.setLabel("现行");
        statusOptions.add(status1);
        
        FilterOptionsDTO.StatusOptionDTO status2 = new FilterOptionsDTO.StatusOptionDTO();
        status2.setValue("废止");
        status2.setLabel("废止");
        statusOptions.add(status2);
        filterOptions.setStatusOptions(statusOptions);
        
        // 备案日期选项
        List<FilterOptionsDTO.RecordDateOptionDTO> recordDateOptions = new ArrayList<>();
        recordDateOptions.add(createRecordDateOption("LAST_MONTH", "近一月"));
        recordDateOptions.add(createRecordDateOption("LAST_THREE_MONTHS", "近三月"));
        recordDateOptions.add(createRecordDateOption("LAST_HALF_YEAR", "近半年"));
        recordDateOptions.add(createRecordDateOption("LAST_YEAR", "近一年"));
        recordDateOptions.add(createRecordDateOption("LAST_TWO_YEARS", "近两年"));
        recordDateOptions.add(createRecordDateOption("LAST_THREE_YEARS", "近三年"));
        filterOptions.setRecordDateOptions(recordDateOptions);
        
        log.info("筛选条件选项获取完成，省份数量：{}", provinces.size());
        
        return filterOptions;
    }

    /**
     * 计算备案日期的起始时间戳
     * @param recordDateType 备案日期类型
     * @return 起始时间戳（毫秒）
     */
    private Long calculateRecordDateStartTimestamp(String recordDateType) {
        LocalDate now = LocalDate.now();
        LocalDate startDate = null;
        
        switch (recordDateType) {
            case "LAST_MONTH":
                startDate = now.minusMonths(1);
                break;
            case "LAST_THREE_MONTHS":
                startDate = now.minusMonths(3);
                break;
            case "LAST_HALF_YEAR":
                startDate = now.minusMonths(6);
                break;
            case "LAST_YEAR":
                startDate = now.minusYears(1);
                break;
            case "LAST_TWO_YEARS":
                startDate = now.minusYears(2);
                break;
            case "LAST_THREE_YEARS":
                startDate = now.minusYears(3);
                break;
            default:
                log.warn("未知的备案日期类型：{}", recordDateType);
                return null;
        }
        
        if (startDate != null) {
            Instant instant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            return instant.toEpochMilli();
        }
        
        return null;
    }

    /**
     * 创建备案日期选项
     */
    private FilterOptionsDTO.RecordDateOptionDTO createRecordDateOption(String value, String label) {
        FilterOptionsDTO.RecordDateOptionDTO option = new FilterOptionsDTO.RecordDateOptionDTO();
        option.setValue(value);
        option.setLabel(label);
        return option;
    }

    /**
     * 转换为记录DTO
     */
    private LocalStandardRecordDTO convertToRecordDTO(LocalStandardDetail detail) {
        LocalStandardRecordDTO dto = new LocalStandardRecordDTO();
        dto.setPk(detail.getPk());
        dto.setCode(detail.getCode());
        dto.setChName(detail.getChName());
        dto.setCity(detail.getCity());
        dto.setStatus(detail.getStatus());
        dto.setIssueDate(detail.getIssueDate());
        dto.setActDate(detail.getActDate());
        dto.setRecordNo(detail.getRecordNo());
        dto.setRecordDate(detail.getRecordDate());
        return dto;
    }
}
