package com.pig4cloud.pig.admin.task;

import com.pig4cloud.pig.admin.entity.IndustryStandardDetail;
import com.pig4cloud.pig.admin.service.IndustryStandardDetailService;
import com.pig4cloud.pig.admin.service.StandardDocumentDownloadService;
import com.pig4cloud.pig.admin.service.StandardDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 标准文档下载定时任务
 *
 * @author pig
 * @date 2025-10-15
 */
@Slf4j
@Component("StandardDocumentDownloader")
@RequiredArgsConstructor
public class StandardDocumentDownloadTask {

    private final StandardDocumentService standardDocumentService;
    private final IndustryStandardDetailService industryStandardDetailService;
    private final StandardDocumentDownloadService downloadService;

    @Value("${standard.download.batch-size:50}")
    private int batchSize;

    @Value("${standard.download.retry-count:3}")
    private int maxRetries;

    @Value("${standard.download.delay-seconds:2}")
    private int delaySeconds;

    /**
     * 下载所有标准文档
     */
    public String downloadAllDocuments() {
        log.info("开始执行标准文档下载任务");
        
        try {
            // 1. 获取需要下载的标准总数
            long totalCount = standardDocumentService.getCountNeedingDownload();
            log.info("找到 {} 个待下载的标准", totalCount);

            if (totalCount == 0) {
                return "没有待下载的标准";
            }

            // 2. 分批处理
            int totalBatches = (int) ((totalCount + batchSize - 1) / batchSize);
            int successCount = 0;
            int failCount = 0;
            int processedCount = 0;

            for (int i = 0; i < totalCount; i += batchSize) {
                int currentBatch = (i / batchSize) + 1;
                int offset = i;
                int limit = batchSize;

                log.info("处理第 {}/{} 批，偏移量: {}, 限制: {}", currentBatch, totalBatches, offset, limit);

                // 获取当前批次的标准PK列表
                List<String> pkList = standardDocumentService.getPksNeedingDownload(offset, limit);
                log.info("第 {} 批获取到 {} 个标准PK", currentBatch, pkList.size());

                for (String pk : pkList) {
                    try {
                        // 根据PK获取标准详情
                        IndustryStandardDetail detail = industryStandardDetailService.lambdaQuery()
                            .eq(IndustryStandardDetail::getPk, pk)
                            .one();

                        if (detail == null) {
                            log.warn("未找到PK为 {} 的标准详情", pk);
                            failCount++;
                            continue;
                        }

                        // 下载文档
                        if (downloadService.downloadDocumentWithRetry(detail, maxRetries)) {
                            successCount++;
                        } else {
                            failCount++;
                        }

                        processedCount++;

                        // 请求间隔
                        if (delaySeconds > 0) {
                            Thread.sleep(delaySeconds * 1000);
                        }

                        // 每处理100个记录输出一次进度
                        if (processedCount % 100 == 0) {
                            log.info("已处理 {} 个标准，成功: {}, 失败: {}", processedCount, successCount, failCount);
                        }

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("下载任务被中断");
                        break;
                    } catch (Exception e) {
                        log.error("处理标准 {} 时发生异常", pk, e);
                        failCount++;
                    }
                }

                // 批次间延迟
                if (currentBatch < totalBatches) {
                    log.info("批次 {} 完成，等待下一批次...", currentBatch);
                    Thread.sleep(5000); // 5秒批次间隔
                }
            }

            String result = String.format("下载任务完成：成功 %d 个，失败 %d 个，总计 %d 个", 
                successCount, failCount, processedCount);
            log.info(result);
            return result;

        } catch (Exception e) {
            log.error("执行标准文档下载任务失败", e);
            return "下载任务执行失败: " + e.getMessage();
        }
    }

    /**
     * 重试失败的下载任务
     */
    public String retryFailedDownloads() {
        log.info("开始执行失败下载任务重试");
        
        try {
            // 获取需要重试的标准总数
            long totalCount = standardDocumentService.getCountNeedingRetry(maxRetries);
            log.info("找到 {} 个需要重试的标准", totalCount);

            if (totalCount == 0) {
                return "没有需要重试的下载任务";
            }

            int successCount = 0;
            int failCount = 0;
            int processedCount = 0;

            for (int i = 0; i < totalCount; i += batchSize) {
                int offset = i;
                int limit = batchSize;

                // 获取当前批次需要重试的标准PK列表
                List<String> pkList = standardDocumentService.getPksNeedingRetry(offset, limit, maxRetries);
                log.info("获取到 {} 个需要重试的标准PK", pkList.size());

                for (String pk : pkList) {
                    try {
                        // 根据PK获取标准详情
                        IndustryStandardDetail detail = industryStandardDetailService.lambdaQuery()
                            .eq(IndustryStandardDetail::getPk, pk)
                            .one();

                        if (detail == null) {
                            log.warn("未找到PK为 {} 的标准详情", pk);
                            failCount++;
                            continue;
                        }

                        // 重试下载文档
                        if (downloadService.downloadDocumentWithRetry(detail, maxRetries)) {
                            successCount++;
                        } else {
                            failCount++;
                        }

                        processedCount++;

                        // 请求间隔
                        if (delaySeconds > 0) {
                            Thread.sleep(delaySeconds * 1000);
                        }

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("重试任务被中断");
                        break;
                    } catch (Exception e) {
                        log.error("重试标准 {} 时发生异常", pk, e);
                        failCount++;
                    }
                }
            }

            String result = String.format("重试任务完成：成功 %d 个，失败 %d 个，总计 %d 个", 
                successCount, failCount, processedCount);
            log.info(result);
            return result;

        } catch (Exception e) {
            log.error("执行失败下载任务重试失败", e);
            return "重试任务执行失败: " + e.getMessage();
        }
    }
}
