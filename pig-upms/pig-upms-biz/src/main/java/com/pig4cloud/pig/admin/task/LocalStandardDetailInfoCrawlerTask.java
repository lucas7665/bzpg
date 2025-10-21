package com.pig4cloud.pig.admin.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pig4cloud.pig.admin.entity.LocalStandardDetail;
import com.pig4cloud.pig.admin.entity.LocalStandardDetailInfo;
import com.pig4cloud.pig.admin.service.LocalStandardDetailInfoCrawlerService;
import com.pig4cloud.pig.admin.service.LocalStandardDetailService;
import com.pig4cloud.pig.admin.service.LocalStandardDetailInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
    private final LocalStandardDetailInfoService localStandardDetailInfoService;

    @Value("${local-standard.crawl.batch-size:50}")
    private int batchSize;

    @Value("${local-standard.crawl.delay-seconds:2}")
    private int delaySeconds;

    /**
     * 按城市分批爬取地方标准详细信息（推荐）
     * 分城市处理可以更好地控制进度，便于并发和断点续传
     */
    public String crawlLocalStandardDetailInfosByCity() {
        log.info("开始执行地方标准详细信息爬取任务（按城市分批）");
        
        try {
            // 获取所有需要爬取的城市代码
            List<String> cityCodes = localStandardDetailService.getCityCodesNeedingDetailInfo();
            log.info("找到 {} 个城市需要爬取详细信息", cityCodes.size());
            
            if (cityCodes.isEmpty()) {
                return "没有需要爬取详细信息的城市";
            }
            
            int totalSuccess = 0;
            int totalFail = 0;
            int cityIndex = 0;
            
            // 按城市逐个处理
            for (String cityCode : cityCodes) {
                cityIndex++;
                long cityCount = localStandardDetailService.getCountNeedingDetailInfoByCity(cityCode);
                log.info("========== 处理城市 [{}/{}]: {} (共{}条) ==========", 
                    cityIndex, cityCodes.size(), cityCode, cityCount);
                
                if (cityCount == 0) {
                    continue;
                }
                
                int successCount = 0;
                int failCount = 0;
                int processedBatches = 0;
                
                // 循环处理该城市的所有批次
                while (true) {
                    // 获取当前城市的一批待处理数据
                    List<String> pks = localStandardDetailService.getPksNeedingDetailInfoByCity(cityCode, 0, batchSize);
                    
                    if (pks.isEmpty()) {
                        log.info("城市 {} 所有数据处理完成", cityCode);
                        break;
                    }
                    
                    List<LocalStandardDetail> batch = localStandardDetailService.list(
                        new LambdaQueryWrapper<LocalStandardDetail>()
                            .in(LocalStandardDetail::getPk, pks)
                    );
                    
                    processedBatches++;
                    log.info("城市 {} - 处理第 {} 批，共 {} 个标准", cityCode, processedBatches, batch.size());
                    
                    // 收集本批次成功爬取的数据
                    List<LocalStandardDetailInfo> batchInfoList = new ArrayList<>();
                    
                    for (LocalStandardDetail detail : batch) {
                        try {
                            LocalStandardDetailInfo info = detailInfoCrawlerService.crawlStandardDetailInfo(detail);
                            if (info != null) {
                                if (info.getPk() == null) {
                                    info.setPk(detail.getPk());
                                }
                                
                                batchInfoList.add(info);
                                log.debug("标准 {} 详细信息爬取成功", detail.getPk());
                                successCount++;
                            } else {
                                log.warn("标准 {} 详细信息爬取失败", detail.getPk());
                                failCount++;
                            }
                            
                            // 请求间隔
                            if (delaySeconds > 0) {
                                Thread.sleep((long)(delaySeconds * 1000));
                            }
                            
                        } catch (Exception e) {
                            log.error("爬取标准 {} 详细信息失败", detail.getPk(), e);
                            failCount++;
                        }
                    }
                    
                    // 批量插入本批次数据
                    if (!batchInfoList.isEmpty()) {
                        try {
                            localStandardDetailInfoService.saveBatch(batchInfoList);
                            log.info("城市 {} - 批次 {} 数据批量插入成功，共 {} 条", cityCode, processedBatches, batchInfoList.size());
                        } catch (Exception e) {
                            log.error("城市 {} - 批次 {} 数据批量插入失败", cityCode, processedBatches, e);
                            // 降级为逐个插入
                            log.info("降级为逐个插入...");
                            for (LocalStandardDetailInfo info : batchInfoList) {
                                try {
                                    localStandardDetailInfoService.save(info);
                                } catch (Exception ex) {
                                    log.error("插入标准 {} 详细信息失败", info.getPk(), ex);
                                }
                            }
                        }
                    }
                    
                    // 批次间延迟
                    Thread.sleep(2000); // 2秒批次间隔
                }
                
                totalSuccess += successCount;
                totalFail += failCount;
                
                log.info("城市 {} 处理完成：成功 {} 个，失败 {} 个", cityCode, successCount, failCount);
            }
            
            String result = String.format("地方标准详细信息爬取完成：共处理%d个城市，成功 %d 个，失败 %d 个，总计 %d 个", 
                cityCodes.size(), totalSuccess, totalFail, (totalSuccess + totalFail));
            log.info(result);
            return result;
            
        } catch (Exception e) {
            log.error("执行地方标准详细信息爬取任务失败", e);
            return "地方标准详细信息爬取失败: " + e.getMessage();
        }
    }

    /**
     * 爬取地方标准详细信息（全局模式）
     */
    public String crawlLocalStandardDetailInfos() {
        log.info("开始执行地方标准详细信息爬取任务");

        try {
            // 先获取总数
            long totalCount = localStandardDetailService.getCountNeedingDetailInfo();
            log.info("找到 {} 个需要爬取详细信息的标准", totalCount);

            if (totalCount == 0) {
                return "没有需要爬取详细信息的标准";
            }

            int successCount = 0;
            int failCount = 0;
            int processedBatches = 0;

            // 循环处理所有批次，直到没有更多数据
            while (true) {
                // 每次获取一批待处理的数据
                List<LocalStandardDetail> batch = getPendingDetailInfoList();

                if (batch.isEmpty()) {
                    log.info("所有数据处理完成");
                    break;
                }

                processedBatches++;
                log.info("处理第 {} 批，共 {} 个标准", processedBatches, batch.size());

                // 收集本批次成功爬取的数据
                List<LocalStandardDetailInfo> batchInfoList = new ArrayList<>();

                for (LocalStandardDetail detail : batch) {
                    try {
                        LocalStandardDetailInfo info = detailInfoCrawlerService.crawlStandardDetailInfo(detail);
                        if (info != null) {
                            // 填充PK（防止实现里未赋值）
                            if (info.getPk() == null) {
                                info.setPk(detail.getPk());
                            }

                            batchInfoList.add(info);
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

                // 批量插入本批次数据
                // 由于查询条件确保这些PK在detail_info表中不存在，直接批量插入即可
                if (!batchInfoList.isEmpty()) {
                    try {
                        localStandardDetailInfoService.saveBatch(batchInfoList);
                        log.info("批次 {} 数据批量插入成功，共 {} 条", processedBatches, batchInfoList.size());
                    } catch (Exception e) {
                        log.error("批次 {} 数据批量插入失败", processedBatches, e);
                        // 批量插入失败时，降级为逐个插入
                        log.info("降级为逐个插入...");
                        for (LocalStandardDetailInfo info : batchInfoList) {
                            try {
                                localStandardDetailInfoService.save(info);
                            } catch (Exception ex) {
                                log.error("插入标准 {} 详细信息失败", info.getPk(), ex);
                            }
                        }
                    }
                }

                // 批次间延迟
                log.info("批次 {} 完成，等待下一批次...", processedBatches);
                Thread.sleep(5000); // 5秒批次间隔
            }

            String result = String.format("地方标准详细信息爬取完成：成功 %d 个，失败 %d 个，总计 %d 个",
                successCount, failCount, (successCount + failCount));
            log.info(result);
            return result;

        } catch (Exception e) {
            log.error("执行地方标准详细信息爬取任务失败", e);
            return "地方标准详细信息爬取失败: " + e.getMessage();
        }
    }

    /**
     * 获取一批需要爬取详细信息的标准列表
     * 每次返回 batchSize 条数据
     * 查询条件：local_standard_detail表中存在但local_standard_detail_info表中不存在的记录
     */
    private List<LocalStandardDetail> getPendingDetailInfoList() {
        // 每次从头开始查询，因为前一批已经插入到 detail_info 表中，会被自动过滤掉
        List<String> pks = localStandardDetailService.getPksNeedingDetailInfo(0, batchSize);
        if (pks.isEmpty()) {
            return List.of();
        }

        // 使用PK字段查询，而不是ID
        return localStandardDetailService.list(
            new LambdaQueryWrapper<LocalStandardDetail>()
                .in(LocalStandardDetail::getPk, pks)
        );
    }
}
