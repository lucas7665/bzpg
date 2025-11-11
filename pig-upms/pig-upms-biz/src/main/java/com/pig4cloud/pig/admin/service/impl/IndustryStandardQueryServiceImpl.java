package com.pig4cloud.pig.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.admin.dto.IndustryStandardDetailResponseDTO;
import com.pig4cloud.pig.admin.dto.IndustryStandardFilterOptionsDTO;
import com.pig4cloud.pig.admin.dto.IndustryStandardQueryDTO;
import com.pig4cloud.pig.admin.dto.IndustryStandardQueryResponseDTO;
import com.pig4cloud.pig.admin.dto.IndustryStandardRecordDTO;
import com.pig4cloud.pig.admin.entity.IndustryCategory;
import com.pig4cloud.pig.admin.entity.IndustryStandardDetail;
import com.pig4cloud.pig.admin.entity.IndustryStandardDetailInfo;
import com.pig4cloud.pig.admin.mapper.IndustryStandardDetailMapper;
import com.pig4cloud.pig.admin.service.IndustryCategoryService;
import com.pig4cloud.pig.admin.service.IndustryStandardDetailInfoService;
import com.pig4cloud.pig.admin.service.IndustryStandardQueryService;
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
 * 行业标准查询Service实现
 *
 * @author pig
 * @date 2025-01-XX
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IndustryStandardQueryServiceImpl extends ServiceImpl<IndustryStandardDetailMapper, IndustryStandardDetail> 
        implements IndustryStandardQueryService {

    private final IndustryCategoryService categoryService;

    private final IndustryStandardDetailInfoService detailInfoService;

    @Override
    public IndustryStandardQueryResponseDTO queryIndustryStandards(Page<IndustryStandardDetail> page, IndustryStandardQueryDTO queryDTO) {
        log.info("分页查询行业标准，查询条件：{}", queryDTO);
        
        LambdaQueryWrapper<IndustryStandardDetail> wrapper = new LambdaQueryWrapper<>();
        
        // 关键词查询（标准号或标准名称）
        if (StrUtil.isNotBlank(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like(IndustryStandardDetail::getCode, queryDTO.getKeyword())
                    .or()
                    .like(IndustryStandardDetail::getChName, queryDTO.getKeyword()));
        }
        
        // 行业代码筛选
        if (StrUtil.isNotBlank(queryDTO.getIndustryCode())) {
            wrapper.eq(IndustryStandardDetail::getIndustryCode, queryDTO.getIndustryCode());
        }
        
        // 部委筛选
        if (StrUtil.isNotBlank(queryDTO.getChargeDept())) {
            wrapper.eq(IndustryStandardDetail::getChargeDept, queryDTO.getChargeDept());
        }
        
        // 备案日期筛选
        if (StrUtil.isNotBlank(queryDTO.getRecordDateType())) {
            Long startTimestamp = calculateRecordDateStartTimestamp(queryDTO.getRecordDateType());
            if (startTimestamp != null) {
                wrapper.ge(IndustryStandardDetail::getRecordDate, startTimestamp);
            }
        }
        
        // 状态筛选
        if (StrUtil.isNotBlank(queryDTO.getStatus())) {
            wrapper.eq(IndustryStandardDetail::getStatus, queryDTO.getStatus());
        }
        
        // 按创建时间倒序排列
        wrapper.orderByDesc(IndustryStandardDetail::getCreateTime);
        
        // 执行分页查询
        Page<IndustryStandardDetail> resultPage = page(page, wrapper);
        
        // 转换为响应DTO
        IndustryStandardQueryResponseDTO responseDTO = new IndustryStandardQueryResponseDTO();
        responseDTO.setTotal(resultPage.getTotal());
        responseDTO.setSize(resultPage.getSize());
        responseDTO.setCurrent(resultPage.getCurrent());
        responseDTO.setPages(resultPage.getPages());
        
        // 转换记录列表
        List<IndustryStandardRecordDTO> records = resultPage.getRecords().stream()
                .map(this::convertToRecordDTO)
                .collect(Collectors.toList());
        responseDTO.setRecords(records);
        
        log.info("查询完成，共找到 {} 条记录，当前页：{}，每页：{}", 
                resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize());
        
        return responseDTO;
    }

    @Override
    public IndustryStandardFilterOptionsDTO getFilterOptions() {
        log.info("获取行业标准筛选条件选项");
        
        IndustryStandardFilterOptionsDTO filterOptions = new IndustryStandardFilterOptionsDTO();
        
        // 获取行业代码列表（带数量统计）
        List<IndustryCategory> categories = categoryService.list();
        List<IndustryStandardFilterOptionsDTO.IndustryCodeOptionDTO> industryCodes = categories.stream()
                .map(category -> {
                    IndustryStandardFilterOptionsDTO.IndustryCodeOptionDTO option = 
                            new IndustryStandardFilterOptionsDTO.IndustryCodeOptionDTO();
                    option.setIndustryCode(category.getIndustryCode());
                    option.setIndustryName(category.getIndustryName());
                    // 实时统计该行业的标准数量
                    LambdaQueryWrapper<IndustryStandardDetail> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(IndustryStandardDetail::getIndustryCode, category.getIndustryCode());
                    long count = count(wrapper);
                    option.setStandardCount((int) count);
                    return option;
                })
                .collect(Collectors.toList());
        filterOptions.setIndustryCodes(industryCodes);
        
        // 获取部委列表（带数量统计）
        // 从industry_standard_detail表中去重获取所有部委
        LambdaQueryWrapper<IndustryStandardDetail> deptWrapper = new LambdaQueryWrapper<>();
        deptWrapper.select(IndustryStandardDetail::getChargeDept)
                .isNotNull(IndustryStandardDetail::getChargeDept)
                .ne(IndustryStandardDetail::getChargeDept, "");
        List<IndustryStandardDetail> allDetails = list(deptWrapper);
        
        // 在Java代码中去重部委名称
        List<String> distinctDepts = allDetails.stream()
                .map(IndustryStandardDetail::getChargeDept)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        
        List<IndustryStandardFilterOptionsDTO.ChargeDeptOptionDTO> chargeDepts = distinctDepts.stream()
                .map(dept -> {
                    IndustryStandardFilterOptionsDTO.ChargeDeptOptionDTO option = 
                            new IndustryStandardFilterOptionsDTO.ChargeDeptOptionDTO();
                    option.setChargeDept(dept);
                    // 统计该部委的标准数量
                    LambdaQueryWrapper<IndustryStandardDetail> countWrapper = new LambdaQueryWrapper<>();
                    countWrapper.eq(IndustryStandardDetail::getChargeDept, dept);
                    long count = count(countWrapper);
                    option.setStandardCount((int) count);
                    return option;
                })
                .sorted((a, b) -> Integer.compare(b.getStandardCount(), a.getStandardCount())) // 按数量降序
                .collect(Collectors.toList());
        filterOptions.setChargeDepts(chargeDepts);
        
        // 状态选项
        List<IndustryStandardFilterOptionsDTO.StatusOptionDTO> statusOptions = new ArrayList<>();
        IndustryStandardFilterOptionsDTO.StatusOptionDTO status1 = 
                new IndustryStandardFilterOptionsDTO.StatusOptionDTO();
        status1.setValue("现行");
        status1.setLabel("现行");
        statusOptions.add(status1);
        
        IndustryStandardFilterOptionsDTO.StatusOptionDTO status2 = 
                new IndustryStandardFilterOptionsDTO.StatusOptionDTO();
        status2.setValue("废止");
        status2.setLabel("废止");
        statusOptions.add(status2);
        filterOptions.setStatusOptions(statusOptions);
        
        // 备案日期选项
        List<IndustryStandardFilterOptionsDTO.RecordDateOptionDTO> recordDateOptions = new ArrayList<>();
        recordDateOptions.add(createRecordDateOption("LAST_MONTH", "近一月"));
        recordDateOptions.add(createRecordDateOption("LAST_THREE_MONTHS", "近三月"));
        recordDateOptions.add(createRecordDateOption("LAST_HALF_YEAR", "近半年"));
        recordDateOptions.add(createRecordDateOption("LAST_YEAR", "近一年"));
        recordDateOptions.add(createRecordDateOption("LAST_TWO_YEARS", "近两年"));
        recordDateOptions.add(createRecordDateOption("LAST_THREE_YEARS", "近三年"));
        filterOptions.setRecordDateOptions(recordDateOptions);
        
        log.info("筛选条件选项获取完成，行业代码数量：{}，部委数量：{}", 
                industryCodes.size(), chargeDepts.size());
        
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
    private IndustryStandardFilterOptionsDTO.RecordDateOptionDTO createRecordDateOption(String value, String label) {
        IndustryStandardFilterOptionsDTO.RecordDateOptionDTO option = 
                new IndustryStandardFilterOptionsDTO.RecordDateOptionDTO();
        option.setValue(value);
        option.setLabel(label);
        return option;
    }

    /**
     * 转换为记录DTO
     */
    private IndustryStandardRecordDTO convertToRecordDTO(IndustryStandardDetail detail) {
        IndustryStandardRecordDTO dto = new IndustryStandardRecordDTO();
        dto.setPk(detail.getPk());
        dto.setCode(detail.getCode());
        dto.setChName(detail.getChName());
        dto.setIndustry(detail.getIndustry());
        dto.setChargeDept(detail.getChargeDept());
        dto.setStatus(detail.getStatus());
        dto.setIssueDate(detail.getIssueDate());
        dto.setActDate(detail.getActDate());
        dto.setRecordNo(detail.getRecordNo());
        dto.setRecordDate(detail.getRecordDate());
        return dto;
    }

    @Override
    public IndustryStandardDetailResponseDTO getStandardDetail(String pk, String code) {
        log.info("查询行业标准详情，pk: {}, code: {}", pk, code);

        // 参数校验
        if (StrUtil.isBlank(pk) && StrUtil.isBlank(code)) {
            log.warn("查询标准详情失败：pk和code都为空");
            return null;
        }

        // 1. 查询基础信息表
        IndustryStandardDetail detail = null;
        if (StrUtil.isNotBlank(pk)) {
            // 优先使用pk查询
            LambdaQueryWrapper<IndustryStandardDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(IndustryStandardDetail::getPk, pk);
            detail = getOne(wrapper);
        } else if (StrUtil.isNotBlank(code)) {
            // 使用code查询
            LambdaQueryWrapper<IndustryStandardDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(IndustryStandardDetail::getCode, code);
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
        IndustryStandardDetailInfo detailInfo = null;
        if (StrUtil.isNotBlank(actualPk)) {
            LambdaQueryWrapper<IndustryStandardDetailInfo> infoWrapper = new LambdaQueryWrapper<>();
            infoWrapper.eq(IndustryStandardDetailInfo::getPk, actualPk);
            detailInfo = detailInfoService.getOne(infoWrapper);
        }

        // 3. 合并数据并转换为响应DTO
        return convertToDetailResponseDTO(detail, detailInfo);
    }

    /**
     * 转换为详情响应DTO
     */
    private IndustryStandardDetailResponseDTO convertToDetailResponseDTO(
            IndustryStandardDetail detail, IndustryStandardDetailInfo detailInfo) {
        IndustryStandardDetailResponseDTO responseDTO = new IndustryStandardDetailResponseDTO();

        // 基本信息
        responseDTO.setPk(detail.getPk());
        responseDTO.setCode(detail.getCode());
        responseDTO.setChName(detail.getChName());
        responseDTO.setIndustry(detail.getIndustry());
        responseDTO.setStatus(detail.getStatus());
        responseDTO.setChargeDept(detail.getChargeDept());
        responseDTO.setReviseStdCodes(detail.getReviseStdCodes());

        // 标准状态信息
        IndustryStandardDetailResponseDTO.StandardStatusDTO standardStatus = 
                new IndustryStandardDetailResponseDTO.StandardStatusDTO();
        
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
        IndustryStandardDetailResponseDTO.BasicInfoDTO basicInfo = 
                new IndustryStandardDetailResponseDTO.BasicInfoDTO();
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
            basicInfo.setIndustryClassification(detailInfo.getIndustryClassification());
            basicInfo.setStandardCategory(detailInfo.getStandardCategory());
        }
        responseDTO.setBasicInfo(basicInfo);

        // 备案信息
        IndustryStandardDetailResponseDTO.RecordInfoDTO recordInfo = 
                new IndustryStandardDetailResponseDTO.RecordInfoDTO();
        
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

