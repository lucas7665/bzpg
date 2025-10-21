package com.pig4cloud.pig.admin.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.pig4cloud.pig.admin.entity.LocalStandardDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 地方标准详情爬取服务
 *
 * @author pig
 * @date 2025-10-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocalStandardDetailCrawlerService {

    private static final String STD_QUERY_URL = "https://dbba.sacinfo.org.cn/stdQueryList";

    /**
     * 爬取指定城市的标准详情
     */
    public List<LocalStandardDetail> crawlStandardDetailsByCity(String cityCode) {
        log.info("开始爬取城市 {} 的标准详情", cityCode);
        
        List<LocalStandardDetail> allStandards = new ArrayList<>();
        int currentPage = 1;
        int totalPages = 1;
        
        try {
            do {
                log.debug("爬取城市 {} 第 {} 页数据", cityCode, currentPage);
                
                // 1. 构建查询参数
                Map<String, Object> params = buildQueryParams(cityCode, currentPage, 100);
                
                // 2. 发送POST请求
                String response = HttpUtil.post(STD_QUERY_URL, params);
                if (StrUtil.isBlank(response)) {
                    log.warn("城市 {} 第 {} 页数据为空", cityCode, currentPage);
                    break;
                }
                
                // 3. 解析响应数据
                StandardQueryResponse queryResponse = JSONUtil.toBean(response, StandardQueryResponse.class);
                if (queryResponse == null || queryResponse.getRecords() == null) {
                    log.warn("城市 {} 第 {} 页响应数据解析失败", cityCode, currentPage);
                    break;
                }
                
                // 4. 转换为实体对象
                List<LocalStandardDetail> pageStandards = convertToEntities(queryResponse.getRecords(), cityCode);
                allStandards.addAll(pageStandards);
                
                // 5. 更新分页信息
                totalPages = queryResponse.getPages();
                currentPage++;
                
                // 6. 延迟避免请求过快
                Thread.sleep(2000);
                
            } while (currentPage <= totalPages);
            
            log.info("城市 {} 爬取完成，共 {} 条标准", cityCode, allStandards.size());
            
        } catch (Exception e) {
            log.error("爬取城市 {} 标准详情失败", cityCode, e);
        }
        
        return allStandards;
    }

    /**
     * 构建查询参数
     */
    private Map<String, Object> buildQueryParams(String cityCode, int current, int size) {
        Map<String, Object> params = new HashMap<>();
        params.put("current", current);
        params.put("size", size);
        params.put("ministry", cityCode);  // 关键差异：使用ministry而不是industry
        params.put("status", "现行");
        return params;
    }

    /**
     * 转换为实体对象列表
     */
    private List<LocalStandardDetail> convertToEntities(List<StandardRecord> records, String cityCode) {
        List<LocalStandardDetail> details = new ArrayList<>();
        
        for (StandardRecord record : records) {
            LocalStandardDetail detail = new LocalStandardDetail();
            
            // 基础字段映射
            detail.setPk(record.getPk());
            detail.setCode(record.getCode());
            detail.setChName(record.getChName());
            detail.setStatus(record.getStatus());
            
            // 地标特有字段
            detail.setCity(record.getIndustry()); // industry字段在地标中存储城市名
            detail.setChargeDept(record.getChargeDept());
            
            // 设置城市代码（重要！）
            detail.setCityCode(cityCode);
            log.debug("设置标准 {} 的城市代码为: {}", record.getPk(), cityCode);
            
            // 时间字段处理
            detail.setIssueDate(record.getIssueDate());
            detail.setActDate(record.getActDate());
            detail.setRecordDate(record.getRecordDate());
            
            // 其他字段
            detail.setRecordNo(record.getRecordNo());
            detail.setReviseStdCodes(record.getReviseStdCodes());
            detail.setEmpty(record.getEmpty());
            detail.setFzDate(record.getFzDate());
            
            // 设置其他结果列
            if (record.getOtherResultColumns() != null) {
                detail.setOtherResultColumns(JSONUtil.toJsonStr(record.getOtherResultColumns()));
            }
            
            // 设置系统字段
            detail.setCreateBy("system");
            detail.setRemark("地方标准详情");
            
            details.add(detail);
        }
        
        return details;
    }

    /**
     * 标准查询响应类
     */
    public static class StandardQueryResponse {
        private int current;
        private int pages;
        private int size;
        private int total;
        private List<StandardRecord> records;

        // getter和setter方法
        public int getCurrent() { return current; }
        public void setCurrent(int current) { this.current = current; }
        public int getPages() { return pages; }
        public void setPages(int pages) { this.pages = pages; }
        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }
        public List<StandardRecord> getRecords() { return records; }
        public void setRecords(List<StandardRecord> records) { this.records = records; }
    }

    /**
     * 标准记录类
     */
    public static class StandardRecord {
        private String pk;
        private String code;
        private String chName;
        private String industry;
        private String chargeDept;
        private String status;
        private Long issueDate;
        private Long actDate;
        private Long recordDate;
        private String recordNo;
        private String reviseStdCodes;
        private Boolean empty;
        private Object otherResultColumns;
        private Long fzDate;

        // getter和setter方法
        public String getPk() { return pk; }
        public void setPk(String pk) { this.pk = pk; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getChName() { return chName; }
        public void setChName(String chName) { this.chName = chName; }
        public String getIndustry() { return industry; }
        public void setIndustry(String industry) { this.industry = industry; }
        public String getChargeDept() { return chargeDept; }
        public void setChargeDept(String chargeDept) { this.chargeDept = chargeDept; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Long getIssueDate() { return issueDate; }
        public void setIssueDate(Long issueDate) { this.issueDate = issueDate; }
        public Long getActDate() { return actDate; }
        public void setActDate(Long actDate) { this.actDate = actDate; }
        public Long getRecordDate() { return recordDate; }
        public void setRecordDate(Long recordDate) { this.recordDate = recordDate; }
        public String getRecordNo() { return recordNo; }
        public void setRecordNo(String recordNo) { this.recordNo = recordNo; }
        public String getReviseStdCodes() { return reviseStdCodes; }
        public void setReviseStdCodes(String reviseStdCodes) { this.reviseStdCodes = reviseStdCodes; }
        public Boolean getEmpty() { return empty; }
        public void setEmpty(Boolean empty) { this.empty = empty; }
        public Object getOtherResultColumns() { return otherResultColumns; }
        public void setOtherResultColumns(Object otherResultColumns) { this.otherResultColumns = otherResultColumns; }
        public Long getFzDate() { return fzDate; }
        public void setFzDate(Long fzDate) { this.fzDate = fzDate; }
    }
}
