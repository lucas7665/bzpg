package com.pig4cloud.pig.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.admin.dto.FilterOptionsDTO;
import com.pig4cloud.pig.admin.dto.LocalStandardDetailResponseDTO;
import com.pig4cloud.pig.admin.dto.LocalStandardQueryDTO;
import com.pig4cloud.pig.admin.dto.LocalStandardQueryResponseDTO;
import com.pig4cloud.pig.admin.dto.LocalStandardRecordDTO;
import com.pig4cloud.pig.admin.entity.LocalStandardCityCategory;
import com.pig4cloud.pig.admin.entity.LocalStandardDetail;
import com.pig4cloud.pig.admin.entity.LocalStandardDetailInfo;
import com.pig4cloud.pig.admin.mapper.LocalStandardDetailMapper;
import com.pig4cloud.pig.admin.service.LocalStandardCityCategoryService;
import com.pig4cloud.pig.admin.service.LocalStandardDetailInfoService;
import com.pig4cloud.pig.admin.service.LocalStandardQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
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

    private final LocalStandardDetailInfoService detailInfoService;

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

    @Override
    public LocalStandardDetailResponseDTO getStandardDetail(String pk, String code) {
        log.info("查询标准详情，pk: {}, code: {}", pk, code);

        // 参数校验
        if (StrUtil.isBlank(pk) && StrUtil.isBlank(code)) {
            log.warn("查询标准详情失败：pk和code都为空");
            return null;
        }

        // 1. 查询基础信息表
        LocalStandardDetail detail = null;
        if (StrUtil.isNotBlank(pk)) {
            // 优先使用pk查询
            LambdaQueryWrapper<LocalStandardDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(LocalStandardDetail::getPk, pk);
            detail = getOne(wrapper);
        } else if (StrUtil.isNotBlank(code)) {
            // 使用code查询
            LambdaQueryWrapper<LocalStandardDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(LocalStandardDetail::getCode, code);
            detail = getOne(wrapper);
        }

        if (detail == null) {
            log.warn("未找到标准，pk: {}, code: {}", pk, code);
            return null;
        }

        // 获取实际的pk（如果通过code查询，需要获取pk）
        String actualPk = detail.getPk();
        log.info("找到标准，pk: {}, code: {}", actualPk, detail.getCode());

        // 2. 查询详细信息表
        LocalStandardDetailInfo detailInfo = null;
        if (StrUtil.isNotBlank(actualPk)) {
            LambdaQueryWrapper<LocalStandardDetailInfo> infoWrapper = new LambdaQueryWrapper<>();
            infoWrapper.eq(LocalStandardDetailInfo::getPk, actualPk);
            detailInfo = detailInfoService.getOne(infoWrapper);
        }

        // 3. 合并数据并转换为响应DTO
        return convertToDetailResponseDTO(detail, detailInfo);
    }

    /**
     * 转换为详情响应DTO
     */
    private LocalStandardDetailResponseDTO convertToDetailResponseDTO(
            LocalStandardDetail detail, LocalStandardDetailInfo detailInfo) {
        LocalStandardDetailResponseDTO responseDTO = new LocalStandardDetailResponseDTO();

        // 基本信息
        responseDTO.setPk(detail.getPk());
        responseDTO.setCode(detail.getCode());
        responseDTO.setChName(detail.getChName());
        responseDTO.setCity(detail.getCity());
        responseDTO.setStatus(detail.getStatus());
        responseDTO.setChargeDept(detail.getChargeDept());
        responseDTO.setReviseStdCodes(detail.getReviseStdCodes());

        // 标准状态信息
        LocalStandardDetailResponseDTO.StandardStatusDTO standardStatus = 
                new LocalStandardDetailResponseDTO.StandardStatusDTO();
        
        // 发布日期：优先使用detailInfo，否则使用detail的时间戳
        String publishDate = null;
        if (detailInfo != null && StrUtil.isNotBlank(detailInfo.getPublishDate())) {
            publishDate = detailInfo.getPublishDate();
        } else if (detail.getIssueDate() != null) {
            publishDate = timestampToDateString(detail.getIssueDate());
        }
        standardStatus.setIssueDate(publishDate);

        // 实施日期：优先使用detailInfo，否则使用detail的时间戳
        String implementDate = null;
        if (detailInfo != null && StrUtil.isNotBlank(detailInfo.getImplementDate())) {
            implementDate = detailInfo.getImplementDate();
        } else if (detail.getActDate() != null) {
            implementDate = timestampToDateString(detail.getActDate());
        }
        standardStatus.setImplementDate(implementDate);

        // 废止日期：使用detail的时间戳
        if (detail.getFzDate() != null) {
            standardStatus.setAbolishDate(timestampToDateString(detail.getFzDate()));
        }
        responseDTO.setStandardStatus(standardStatus);

        // 基础信息
        LocalStandardDetailResponseDTO.BasicInfoDTO basicInfo = 
                new LocalStandardDetailResponseDTO.BasicInfoDTO();
        basicInfo.setCode(detail.getCode());
        
        if (detailInfo != null) {
            basicInfo.setChinaClassification(detailInfo.getChinaClassification());
            basicInfo.setInternationalClassification(detailInfo.getInternationalClassification());
            basicInfo.setPublishDate(publishDate);
            basicInfo.setImplementDate(implementDate);
            basicInfo.setTechnicalCommittee(detailInfo.getTechnicalCommittee());
            basicInfo.setRevisionType(detailInfo.getRevisionType());
            basicInfo.setApprovalDepartment(detailInfo.getApprovalDepartment());
            basicInfo.setReplaceStandard(detailInfo.getReplaceStandard());
            basicInfo.setProposingDepartment(detail.getChargeDept()); // 提出部门使用负责部门
            // 行业分类：优先使用city_classification，如果没有则使用industry_classification
            String industryClassification = StrUtil.isNotBlank(detailInfo.getCityClassification()) 
                    ? detailInfo.getCityClassification() 
                    : null; // 如果表结构中有industry_classification字段，可以在这里添加
            basicInfo.setIndustryClassification(industryClassification);
            basicInfo.setStandardCategory(detailInfo.getStandardCategory());
        }
        responseDTO.setBasicInfo(basicInfo);

        // 备案信息
        LocalStandardDetailResponseDTO.RecordInfoDTO recordInfo = 
                new LocalStandardDetailResponseDTO.RecordInfoDTO();
        
        // 备案号：优先使用detailInfo，否则使用detail
        String recordNumber = null;
        if (detailInfo != null && StrUtil.isNotBlank(detailInfo.getRecordNumber())) {
            recordNumber = detailInfo.getRecordNumber();
        } else if (StrUtil.isNotBlank(detail.getRecordNo())) {
            recordNumber = detail.getRecordNo();
        }
        recordInfo.setRecordNumber(recordNumber);

        // 备案日期：优先使用detailInfo（字符串），否则使用detail（时间戳）
        String recordDate = null;
        if (detailInfo != null && StrUtil.isNotBlank(detailInfo.getRecordDate())) {
            recordDate = detailInfo.getRecordDate();
        } else if (detail.getRecordDate() != null) {
            recordDate = timestampToDateString(detail.getRecordDate());
        }
        recordInfo.setRecordDate(recordDate);

        if (detailInfo != null) {
            recordInfo.setRecordBulletin(detailInfo.getRecordBulletin());
        }
        responseDTO.setRecordInfo(recordInfo);

        // 适用范围
        if (detailInfo != null) {
            responseDTO.setScope(detailInfo.getScope());
        }

        // 起草单位列表
        if (detailInfo != null && StrUtil.isNotBlank(detailInfo.getDraftingUnits())) {
            responseDTO.setDraftingUnits(splitStringToList(detailInfo.getDraftingUnits()));
        } else {
            responseDTO.setDraftingUnits(new ArrayList<>());
        }

        // 起草人列表
        if (detailInfo != null && StrUtil.isNotBlank(detailInfo.getDraftingPersons())) {
            responseDTO.setDraftingPersons(splitStringToList(detailInfo.getDraftingPersons()));
        } else {
            responseDTO.setDraftingPersons(new ArrayList<>());
        }

        return responseDTO;
    }

    /**
     * 时间戳转日期字符串
     * @param timestamp 时间戳（毫秒）
     * @return 日期字符串（yyyy-MM-dd）
     */
    private String timestampToDateString(Long timestamp) {
        if (timestamp == null) {
            return null;
        }
        try {
            return Instant.ofEpochMilli(timestamp)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            log.warn("时间戳转换失败: {}", timestamp, e);
            return null;
        }
    }

    /**
     * 将字符串按分隔符分割为列表
     * @param str 待分割的字符串
     * @return 字符串列表
     */
    private List<String> splitStringToList(String str) {
        if (StrUtil.isBlank(str)) {
            return new ArrayList<>();
        }
        try {
            return Arrays.stream(str.split("[,;，；\\n\\r]+"))
                    .map(String::trim)
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("字符串分割失败: {}", str, e);
            return new ArrayList<>();
        }
    }
}
