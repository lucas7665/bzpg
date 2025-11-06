package com.pig4cloud.pig.admin.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.pig4cloud.pig.admin.dto.StandardQueryResponse;
import com.pig4cloud.pig.admin.dto.StandardRecord;
import com.pig4cloud.pig.admin.entity.LocalStandardDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
                
                // 3. 解析响应数据（使用通用DTO）
                com.pig4cloud.pig.admin.dto.StandardQueryResponse queryResponse = JSONUtil.toBean(response, com.pig4cloud.pig.admin.dto.StandardQueryResponse.class);
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
        return buildQueryParams(cityCode, current, size, false);
    }

    /**
     * 构建查询参数（支持增量爬取）
     * @param cityCode 城市代码，增量爬取时传入空字符串
     * @param current 当前页码
     * @param size 每页大小
     * @param incremental 是否为增量爬取
     * @return 查询参数Map
     */
    private Map<String, Object> buildQueryParams(String cityCode, int current, int size, boolean incremental) {
        Map<String, Object> params = new HashMap<>();
        params.put("current", current);
        params.put("size", size);
        
        if (incremental) {
            // 增量爬取：添加 pubdate=-1 参数（最近一个月），不传 ministry
            params.put("pubdate", -1);
        } else {
            // 全量爬取：传入城市代码
            params.put("ministry", cityCode);  // 关键差异：使用ministry而不是industry
            params.put("status", "现行");
        }
        
        return params;
    }

    /**
     * 增量爬取地方标准数据（最近一个月）
     * @return 标准详情列表
     */
    public List<LocalStandardDetail> crawlIncrementalStandardDetails() {
        List<LocalStandardDetail> allStandards = new ArrayList<>();

        try {
            log.info("开始增量爬取地方标准数据（最近一个月）...");

            // 先获取第一页，了解分页信息
            com.pig4cloud.pig.admin.dto.StandardQueryResponse firstPageResponse = getStandardDetailsByPage("", 1, 100, true);

            if (firstPageResponse == null || firstPageResponse.getRecords() == null) {
                log.warn("最近一个月没有地方标准数据");
                return allStandards;
            }

            // 添加第一页数据
            allStandards.addAll(parseStandardDetails(firstPageResponse.getRecords()));

            int totalPages = firstPageResponse.getPages();
            int totalRecords = firstPageResponse.getTotal();

            log.info("最近一个月共有 {} 页数据，总计 {} 条记录", totalPages, totalRecords);

            // 如果有多页，继续爬取剩余页面
            if (totalPages > 1) {
                for (int currentPage = 2; currentPage <= totalPages; currentPage++) {
                    try {
                        com.pig4cloud.pig.admin.dto.StandardQueryResponse pageResponse = getStandardDetailsByPage("", currentPage, 100, true);
                        if (pageResponse != null && pageResponse.getRecords() != null) {
                            allStandards.addAll(parseStandardDetails(pageResponse.getRecords()));
                            log.info("已爬取第 {}/{} 页数据", currentPage, totalPages);
                        }

                        // 添加延迟，避免请求过于频繁
                        Thread.sleep(500);
                    } catch (Exception e) {
                        log.error("爬取第 {} 页数据失败", currentPage, e);
                        // 单页失败不影响其他页面
                    }
                }
            }

            log.info("增量爬取完成，共获取 {} 条记录", allStandards.size());

        } catch (Exception e) {
            log.error("增量爬取地方标准数据失败", e);
        }

        return allStandards;
    }

    /**
     * 获取指定页的标准数据（支持增量爬取）
     */
    private com.pig4cloud.pig.admin.dto.StandardQueryResponse getStandardDetailsByPage(String cityCode, int currentPage, int pageSize, boolean incremental) {
        try {
            String postData = buildPostData(cityCode, currentPage, pageSize, incremental);

            // 使用 Jsoup 发送 POST 请求（与行业标准保持一致）
            Document doc = Jsoup.connect(STD_QUERY_URL)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .requestBody(postData)
                .timeout(30000)
                .post();

            String jsonResponse = doc.text();
            return JSONUtil.toBean(jsonResponse, com.pig4cloud.pig.admin.dto.StandardQueryResponse.class);

        } catch (Exception e) {
            log.error("获取城市 {} 第 {} 页数据失败", cityCode, currentPage, e);
            return null;
        }
    }

    /**
     * 构建POST请求参数字符串（支持增量爬取）
     */
    private String buildPostData(String cityCode, int currentPage, int pageSize, boolean incremental) {
        if (incremental) {
            // 增量爬取：添加 pubdate=-1 参数（最近一个月），不传 ministry
            return String.format(
                "current=%d&size=%d&pubdate=-1",
                currentPage, pageSize
            );
        } else {
            // 全量爬取：传入城市代码
            return String.format(
                "current=%d&size=%d&ministry=%s&status=现行",
                currentPage, pageSize, cityCode
            );
        }
    }

    /**
     * 解析标准记录为实体对象列表（增量爬取版本，不需要传入cityCode）
     */
    private List<LocalStandardDetail> parseStandardDetails(List<com.pig4cloud.pig.admin.dto.StandardRecord> records) {
        List<LocalStandardDetail> details = new ArrayList<>();

        for (com.pig4cloud.pig.admin.dto.StandardRecord record : records) {
            try {
                LocalStandardDetail detail = new LocalStandardDetail();

                // 基础字段映射
                detail.setPk(record.getPk());
                detail.setCode(record.getCode());
                detail.setChName(record.getChName());
                detail.setStatus(record.getStatus());

                // 地标特有字段
                detail.setCity(record.getIndustry()); // industry字段在地标中存储城市名
                detail.setChargeDept(record.getChargeDept());

                // 注意：增量爬取时，cityCode 需要从返回的数据中解析或后续关联
                // 这里先不设置 cityCode，后续通过城市分类关联来设置

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
                detail.setUpdateBy("system");
                detail.setRemark("地方标准详情（增量爬取）");

                details.add(detail);
            } catch (Exception e) {
                log.warn("解析标准记录失败: {}", record.getPk(), e);
            }
        }

        return details;
    }

    /**
     * 转换为实体对象列表（原有方法，保留兼容性）
     */
    private List<LocalStandardDetail> convertToEntities(List<com.pig4cloud.pig.admin.dto.StandardRecord> records, String cityCode) {
        List<LocalStandardDetail> details = new ArrayList<>();
        
        for (com.pig4cloud.pig.admin.dto.StandardRecord record : records) {
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
