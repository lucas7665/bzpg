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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

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

    @Value("${local-standard.crawl.concurrent-threads:3}")
    private int concurrentThreads;

    /**
     * 按城市分批爬取地方标准详细信息 - 多线程并发版本（推荐）⭐
     * 使用线程池并发处理多个城市，大幅提升效率
     */
    public String crawlLocalStandardDetailInfosByCityMultiThread() {
        log.info("开始执行地方标准详细信息爬取任务（多线程并发）");
        log.info("并发配置：线程数={}, 批次大小={}, 延迟={}秒", concurrentThreads, batchSize, delaySeconds);
        
        try {
            // 获取所有需要爬取的城市代码
            List<String> cityCodes = localStandardDetailService.getCityCodesNeedingDetailInfo();
            log.info("找到 {} 个城市需要爬取详细信息", cityCodes.size());
            
            if (cityCodes.isEmpty()) {
                return "没有需要爬取详细信息的城市";
            }
            
            // 创建线程池
            ThreadPoolExecutor executor = new ThreadPoolExecutor(
                concurrentThreads,           // 核心线程数
                concurrentThreads,           // 最大线程数
                60L,                         // 空闲线程存活时间
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(), // 任务队列
                new ThreadFactory() {
                    private final AtomicInteger threadNumber = new AtomicInteger(1);
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "LocalStandard-Crawler-" + threadNumber.getAndIncrement());
                    }
                }
            );
            
            // 线程安全的计数器
            AtomicInteger totalSuccess = new AtomicInteger(0);
            AtomicInteger totalFail = new AtomicInteger(0);
            AtomicInteger completedCities = new AtomicInteger(0);
            
            // 使用 CountDownLatch 等待所有任务完成
            CountDownLatch latch = new CountDownLatch(cityCodes.size());
            
            // 为每个城市创建并提交任务
            for (String cityCode : cityCodes) {
                executor.submit(() -> {
                    try {
                        log.info("[{}] 开始处理城市: {}", Thread.currentThread().getName(), cityCode);
                        
                        // 处理该城市的所有数据
                        CityProcessResult result = processCityData(cityCode);
                        
                        // 累加结果
                        totalSuccess.addAndGet(result.getSuccessCount());
                        totalFail.addAndGet(result.getFailCount());
                        int completed = completedCities.incrementAndGet();
                        
                        log.info("[{}] 城市 {} 处理完成：成功 {} 个，失败 {} 个 | 总进度: {}/{}", 
                            Thread.currentThread().getName(), cityCode, 
                            result.getSuccessCount(), result.getFailCount(),
                            completed, cityCodes.size());
                        
                    } catch (Exception e) {
                        log.error("[{}] 处理城市 {} 失败", Thread.currentThread().getName(), cityCode, e);
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            // 关闭线程池，不再接受新任务
            executor.shutdown();
            
            // 等待所有任务完成，最多等待24小时
            log.info("等待所有线程完成...");
            boolean finished = latch.await(24, TimeUnit.HOURS);
            
            if (!finished) {
                log.warn("部分任务未在规定时间内完成");
                executor.shutdownNow();
            }
            
            String result = String.format("地方标准详细信息爬取完成（多线程）：处理%d个城市，成功 %d 个，失败 %d 个，总计 %d 个", 
                cityCodes.size(), totalSuccess.get(), totalFail.get(), (totalSuccess.get() + totalFail.get()));
            log.info(result);
            return result;
            
        } catch (Exception e) {
            log.error("执行地方标准详细信息爬取任务失败", e);
            return "地方标准详细信息爬取失败: " + e.getMessage();
        }
    }
    
    /**
     * 处理单个城市的所有数据
     */
    private CityProcessResult processCityData(String cityCode) {
        int successCount = 0;
        int failCount = 0;
        
        try {
            long cityCount = localStandardDetailService.getCountNeedingDetailInfoByCity(cityCode);
            log.info("[{}] 城市 {} 共有 {} 条数据待处理", 
                Thread.currentThread().getName(), cityCode, cityCount);
            
            if (cityCount == 0) {
                return new CityProcessResult(successCount, failCount);
            }
            
            int processedBatches = 0;
            
            // 循环处理该城市的所有批次
            while (true) {
                // 获取当前城市的一批待处理数据
                List<String> pks = localStandardDetailService.getPksNeedingDetailInfoByCity(cityCode, 0, batchSize);
                
                if (pks.isEmpty()) {
                    break;
                }
                
                List<LocalStandardDetail> batch = localStandardDetailService.list(
                    new LambdaQueryWrapper<LocalStandardDetail>()
                        .in(LocalStandardDetail::getPk, pks)
                );
                
                processedBatches++;
                log.debug("[{}] 城市 {} - 处理第 {} 批，共 {} 个标准", 
                    Thread.currentThread().getName(), cityCode, processedBatches, batch.size());
                
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
                            successCount++;
                        } else {
                            failCount++;
                        }
                        
                        // 请求间隔
                        if (delaySeconds > 0) {
                            Thread.sleep((long)(delaySeconds * 1000));
                        }
                        
                    } catch (Exception e) {
                        log.error("[{}] 爬取标准 {} 详细信息失败", 
                            Thread.currentThread().getName(), detail.getPk(), e);
                        failCount++;
                    }
                }
                
                // 批量插入本批次数据
                if (!batchInfoList.isEmpty()) {
                    try {
                        localStandardDetailInfoService.saveBatch(batchInfoList);
                        log.debug("[{}] 城市 {} - 批次 {} 数据批量插入成功，共 {} 条", 
                            Thread.currentThread().getName(), cityCode, processedBatches, batchInfoList.size());
                    } catch (Exception e) {
                        log.error("[{}] 城市 {} - 批次 {} 数据批量插入失败", 
                            Thread.currentThread().getName(), cityCode, processedBatches, e);
                        // 降级为逐个插入
                        for (LocalStandardDetailInfo info : batchInfoList) {
                            try {
                                localStandardDetailInfoService.save(info);
                            } catch (Exception ex) {
                                log.error("[{}] 插入标准 {} 详细信息失败", 
                                    Thread.currentThread().getName(), info.getPk(), ex);
                            }
                        }
                    }
                }
                
                // 批次间延迟
                Thread.sleep(2000);
            }
            
        } catch (Exception e) {
            log.error("[{}] 处理城市 {} 数据时发生异常", 
                Thread.currentThread().getName(), cityCode, e);
        }
        
        return new CityProcessResult(successCount, failCount);
    }
    
    /**
     * 城市处理结果
     */
    private static class CityProcessResult {
        private final int successCount;
        private final int failCount;
        
        public CityProcessResult(int successCount, int failCount) {
            this.successCount = successCount;
            this.failCount = failCount;
        }
        
        public int getSuccessCount() {
            return successCount;
        }
        
        public int getFailCount() {
            return failCount;
        }
    }

    /**
     * 按城市分批爬取地方标准详细信息 - 单线程版本
     * 分城市处理可以更好地控制进度，便于并发和断点续传
     */
    public String crawlLocalStandardDetailInfosByCity() {
        log.info("开始执行地方标准详细信息爬取任务（按城市分批-单线程）");
        
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
