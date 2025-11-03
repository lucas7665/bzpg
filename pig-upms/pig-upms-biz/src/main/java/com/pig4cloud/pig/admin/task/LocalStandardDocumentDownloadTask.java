package com.pig4cloud.pig.admin.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pig4cloud.pig.admin.entity.LocalStandardDetail;
import com.pig4cloud.pig.admin.service.LocalStandardDocumentDownloadService;
import com.pig4cloud.pig.admin.service.LocalStandardDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 地方标准文档下载定时任务
 *
 * @author pig
 * @date 2025-10-15
 */
@Slf4j
@Component("LocalStandardDocumentDownloader")
@RequiredArgsConstructor
public class LocalStandardDocumentDownloadTask {

    private final LocalStandardDocumentDownloadService downloadService;
    private final LocalStandardDetailService localStandardDetailService;

    @Value("${local-standard.download.batch-size:50}")
    private int batchSize;

    @Value("${local-standard.download.retry-count:3}")
    private int maxRetries;

    @Value("${local-standard.download.delay-seconds:2}")
    private int delaySeconds;

    /**
     * 下载所有地方标准文档
     */
    public String downloadAllDocuments() {
        log.info("开始执行地方标准文档下载任务");

        try {
            int successCount = 0;
            int failCount = 0;
            int processedCount = 0;

            int offset = 0;
            int batchIndex = 0;

            while (true) {
                // 分页查询待下载 PK 列表
                List<String> pks = localStandardDetailService.getPksNeedingDownload(offset, batchSize);
                if (pks == null || pks.isEmpty()) {
                    break;
                }

                // 使用 PK 查询详情列表
                List<LocalStandardDetail> batch = localStandardDetailService.list(
                    new LambdaQueryWrapper<LocalStandardDetail>()
                        .in(LocalStandardDetail::getPk, pks)
                );

                batchIndex++;
                log.info("处理第 {} 批，共 {} 个标准", batchIndex, batch.size());

                for (LocalStandardDetail detail : batch) {
                    try {
                        if (downloadService.downloadDocumentWithRetry(detail, maxRetries)) {
                            successCount++;
                        } else {
                            failCount++;
                        }

                        processedCount++;

                        // 单条请求间隔
                        if (delaySeconds > 0) {
                            Thread.sleep(delaySeconds * 1000);
                        }

                        if (processedCount % 100 == 0) {
                            log.info("已处理 {} 个标准，成功: {}, 失败: {}", processedCount, successCount, failCount);
                        }

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("下载任务被中断");
                        break;
                    } catch (Exception e) {
                        log.error("处理标准 {} 时发生异常", detail.getPk(), e);
                        failCount++;
                    }
                }

                // 批次间延迟
                log.info("批次 {} 完成，等待下一批次...", batchIndex);
                Thread.sleep(5000); // 5秒批次间隔

                // 下一页偏移
                offset += batchSize;
            }

            if (processedCount == 0) {
                return "没有待下载的地方标准";
            }

            String result = String.format("地方标准文档下载任务完成：成功 %d 个，失败 %d 个，总计 %d 个",
                successCount, failCount, processedCount);
            log.info(result);
            return result;

        } catch (Exception e) {
            log.error("执行地方标准文档下载任务失败", e);
            return "地方标准文档下载任务执行失败: " + e.getMessage();
        }
    }

    /**
     * 重试失败的地方标准下载任务
     */
    public String retryFailedDownloads() {
        log.info("开始执行失败的地方标准下载任务重试");
        
        try {
            int successCount = 0;
            int failCount = 0;
            int processedCount = 0;

            int offset = 0;
            int batchIndex = 0;

            while (true) {
                // 分页查询需要重试的 PK 列表
                List<String> pks = localStandardDetailService.getPksNeedingRetry(offset, batchSize, maxRetries);
                if (pks == null || pks.isEmpty()) {
                    break;
                }

                List<LocalStandardDetail> batch = localStandardDetailService.list(
                    new LambdaQueryWrapper<LocalStandardDetail>()
                        .in(LocalStandardDetail::getPk, pks)
                );

                batchIndex++;
                log.info("重试处理第 {} 批，共 {} 个标准", batchIndex, batch.size());

                for (LocalStandardDetail detail : batch) {
                    try {
                        if (downloadService.downloadDocumentWithRetry(detail, maxRetries)) {
                            successCount++;
                        } else {
                            failCount++;
                        }

                        processedCount++;

                        if (delaySeconds > 0) {
                            Thread.sleep(delaySeconds * 1000);
                        }

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("重试任务被中断");
                        break;
                    } catch (Exception e) {
                        log.error("重试标准 {} 时发生异常", detail.getPk(), e);
                        failCount++;
                    }
                }

                log.info("重试批次 {} 完成，等待下一批次...", batchIndex);
                Thread.sleep(5000);

                offset += batchSize;
            }

            if (processedCount == 0) {
                return "没有需要重试的地方标准下载任务";
            }

            String result = String.format("地方标准重试任务完成：成功 %d 个，失败 %d 个，总计 %d 个",
                successCount, failCount, processedCount);
            log.info(result);
            return result;

        } catch (Exception e) {
            log.error("执行失败的地方标准下载任务重试失败", e);
            return "地方标准重试任务执行失败: " + e.getMessage();
        }
    }

    /**
     * 获取需要下载的标准列表
     * 从数据库查询需要下载的标准
     */
    private List<LocalStandardDetail> getPendingDownloadList() {
        // 查询条件：local_standard_detail表中存在但local_standard_document表中不存在的记录
        List<String> pks = localStandardDetailService.getPksNeedingDownload(0, batchSize);
        if (pks.isEmpty()) {
            return List.of();
        }
        
        // 使用PK字段查询，而不是ID
        return localStandardDetailService.list(
            new LambdaQueryWrapper<LocalStandardDetail>()
                .in(LocalStandardDetail::getPk, pks)
        );
    }

    /**
     * 获取需要重试下载的标准列表
     * 从数据库查询需要重试下载的标准
     */
    private List<LocalStandardDetail> getRetryDownloadList() {
        // 查询条件：local_standard_document表中下载状态为FAILED且重试次数小于最大重试次数的记录
        List<String> pks = localStandardDetailService.getPksNeedingRetry(0, batchSize, maxRetries);
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
