package com.pig4cloud.pig.admin.task;

import com.pig4cloud.pig.admin.entity.LocalStandardDetail;
import com.pig4cloud.pig.admin.entity.LocalStandardDetailInfo;
import com.pig4cloud.pig.admin.service.LocalStandardDetailInfoCrawlerService;
import com.pig4cloud.pig.admin.service.LocalStandardDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 地方标准详细信息爬取定时任务
 *
 * @author pig
 * @date 2025-10-15
 */
@Slf4j
@Component("LocalStandardDetailInfoCrawler")
@RequiredArgsConstructor
public class LocalStandardDetailInfoCrawlerTask {

    private final LocalStandardDetailInfoCrawlerService detailInfoCrawlerService;
    private final LocalStandardDetailService localStandardDetailService;

    @Value("${local-standard.crawl.batch-size:50}")
    private int batchSize;

    @Value("${local-standard.crawl.delay-seconds:2}")
    private int delaySeconds;

    /**
     * 爬取地方标准详细信息
     */
    public String crawlLocalStandardDetailInfos() {
        log.info("开始执行地方标准详细信息爬取任务");
        
        try {
            // 这里应该从数据库获取需要爬取详细信息的标准列表
            // 暂时使用模拟数据
            List<LocalStandardDetail> pendingList = getPendingDetailInfoList();
            log.info("找到 {} 个需要爬取详细信息的标准", pendingList.size());
            
            if (pendingList.isEmpty()) {
                return "没有需要爬取详细信息的标准";
            }
            
            // 分批处理
            int totalBatches = (pendingList.size() + batchSize - 1) / batchSize;
            int successCount = 0;
            int failCount = 0;
            
            for (int i = 0; i < pendingList.size(); i += batchSize) {
                int end = Math.min(i + batchSize, pendingList.size());
                List<LocalStandardDetail> batch = pendingList.subList(i, end);
                int currentBatch = (i / batchSize) + 1;
                
                log.info("处理第 {}/{} 批，共 {} 个标准", currentBatch, totalBatches, batch.size());
                
                for (LocalStandardDetail detail : batch) {
                    try {
                        LocalStandardDetailInfo info = detailInfoCrawlerService.crawlStandardDetailInfo(detail);
                        if (info != null) {
                            // 这里应该保存到数据库
                            log.debug("标准 {} 详细信息爬取成功", detail.getPk());
                            successCount++;
                        } else {
                            log.warn("标准 {} 详细信息爬取失败", detail.getPk());
                            failCount++;
                        }
                        
                        // 请求间隔
                        if (delaySeconds > 0) {
                            Thread.sleep(delaySeconds * 1000);
                        }
                        
                    } catch (Exception e) {
                        log.error("爬取标准 {} 详细信息失败", detail.getPk(), e);
                        failCount++;
                    }
                }
                
                // 批次间延迟
                if (currentBatch < totalBatches) {
                    log.info("批次 {} 完成，等待下一批次...", currentBatch);
                    Thread.sleep(5000); // 5秒批次间隔
                }
            }
            
            String result = String.format("地方标准详细信息爬取完成：成功 %d 个，失败 %d 个，总计 %d 个", 
                successCount, failCount, pendingList.size());
            log.info(result);
            return result;
            
        } catch (Exception e) {
            log.error("执行地方标准详细信息爬取任务失败", e);
            return "地方标准详细信息爬取失败: " + e.getMessage();
        }
    }

    /**
     * 获取需要爬取详细信息的标准列表
     * 从数据库查询需要爬取详细信息的标准
     */
    private List<LocalStandardDetail> getPendingDetailInfoList() {
        // 查询条件：local_standard_detail表中存在但local_standard_detail_info表中不存在的记录
        List<String> pks = localStandardDetailService.getPksNeedingDetailInfo(0, batchSize);
        if (pks.isEmpty()) {
            return List.of();
        }
        
        return localStandardDetailService.listByIds(pks);
    }
}
