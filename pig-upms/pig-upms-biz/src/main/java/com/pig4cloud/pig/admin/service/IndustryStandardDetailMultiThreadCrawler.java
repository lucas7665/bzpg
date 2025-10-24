package com.pig4cloud.pig.admin.service;

import com.pig4cloud.pig.admin.entity.IndustryCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 行业标准详细信息多线程爬取服务
 *
 * @author pig4cloud
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IndustryStandardDetailMultiThreadCrawler {
    
    private final IndustryStandardDetailInfoService detailInfoService;
    private final IndustryStandardDetailCrawlerService crawlerService;
    private final IndustryCategoryService categoryService;
    
    @Value("${industry-standard.detail.crawl.concurrent-threads:3}")
    private int concurrentThreads;
    
    @Value("${industry-standard.detail.crawl.batch-size:200}")
    private int batchSize;
    
    @Value("${industry-standard.detail.crawl.delay-seconds:0.5}")
    private double delaySeconds;
    
    /**
     * 多线程爬取所有行业标准详细信息
     */
    public String crawlAllStandardDetailsMultiThread() {
        log.info("开始执行行业标准详细信息多线程爬取任务");
        log.info("并发配置：线程数={}, 批次大小={}, 延迟={}秒", 
            concurrentThreads, batchSize, delaySeconds);
        
        try {
            // 1. 获取所有需要爬取的行业分类
            List<IndustryCategory> categories = getCategoriesNeedingDetailInfo();
            log.info("找到 {} 个行业分类需要爬取详细信息", categories.size());
            
            if (categories.isEmpty()) {
                return "没有需要爬取详细信息的行业分类";
            }
            
            // 2. 创建线程池
            ThreadPoolExecutor executor = createThreadPool();
            
            // 3. 线程安全的计数器
            AtomicInteger totalSuccess = new AtomicInteger(0);
            AtomicInteger totalFail = new AtomicInteger(0);
            AtomicInteger completedCategories = new AtomicInteger(0);
            
            // 4. 使用 CountDownLatch 等待所有任务完成
            CountDownLatch latch = new CountDownLatch(categories.size());
            
            // 5. 为每个行业分类创建并提交任务
            for (IndustryCategory category : categories) {
                CompletableFuture.runAsync(() -> {
                    try {
                        processIndustryCategory(category, totalSuccess, totalFail, completedCategories);
                    } finally {
                        latch.countDown();
                    }
                }, executor);
            }
            
            // 6. 等待所有任务完成
            latch.await();
            executor.shutdown();
            
            log.info("行业标准详细信息多线程爬取完成：成功 {} 个，失败 {} 个，总计 {} 个",
                totalSuccess.get(), totalFail.get(), 
                totalSuccess.get() + totalFail.get());
            
            return "0"; // 成功
            
        } catch (Exception e) {
            log.error("行业标准详细信息多线程爬取失败", e);
            return "1"; // 失败
        }
    }
    
    /**
     * 获取需要爬取详细信息的行业分类
     */
    private List<IndustryCategory> getCategoriesNeedingDetailInfo() {
        // 获取所有需要爬取详细信息的行业代码
        List<String> industryCodes = detailInfoService.getIndustryCodesNeedingDetailInfo();
        
        if (industryCodes.isEmpty()) {
            return List.of();
        }
        
        // 根据行业代码查询行业分类信息
        return categoryService.list(
            categoryService.query()
                .in("industry_code", industryCodes)
                .getWrapper()
        );
    }
    
    /**
     * 创建线程池
     */
    private ThreadPoolExecutor createThreadPool() {
        return new ThreadPoolExecutor(
            concurrentThreads,           // 核心线程数
            concurrentThreads,           // 最大线程数
            60L,                         // 空闲线程存活时间
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(), // 任务队列
            new ThreadFactory() {
                private final AtomicInteger threadNumber = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "IndustryDetail-Crawler-" + threadNumber.getAndIncrement());
                }
            }
        );
    }
    
    /**
     * 处理单个行业分类的详细信息爬取
     */
    private void processIndustryCategory(IndustryCategory category, 
                                       AtomicInteger totalSuccess, 
                                       AtomicInteger totalFail,
                                       AtomicInteger completedCategories) {
        
        String threadName = Thread.currentThread().getName();
        String industryCode = category.getIndustryCode();
        String industryName = category.getIndustryName();
        
        try {
            log.info("[{}] 开始处理行业: {} ({})", threadName, industryName, industryCode);
            
            // 1. 获取该行业需要爬取的总数
            long totalCount = detailInfoService.getPksNeedingDetailInfoCountByIndustryCode(industryCode);
            if (totalCount == 0) {
                log.info("[{}] 行业 {} 没有需要爬取的详细信息", threadName, industryName);
                return;
            }
            
            log.info("[{}] 行业 {} 共有 {} 条数据待处理", threadName, industryName, totalCount);
            
            // 2. 分页处理该行业的数据
            int totalProcessed = 0;
            int pageSize = 1000; // 每页1000条
            int currentPage = 0;
            
            while (totalProcessed < totalCount) {
                // 分页获取PK列表
                List<String> pks = detailInfoService.getPksNeedingDetailInfoByIndustryCode(
                    industryCode, currentPage * pageSize, pageSize);
                
                if (pks.isEmpty()) {
                    break;
                }
                
                // 分批处理当前页的数据
                for (int i = 0; i < pks.size(); i += batchSize) {
                    int end = Math.min(i + batchSize, pks.size());
                    List<String> batch = pks.subList(i, end);
                    
                    int batchProcessed = crawlerService.crawlBatchStandardDetails(batch);
                    totalProcessed += batchProcessed;
                    totalSuccess.addAndGet(batchProcessed);
                    
                    log.info("[{}] 行业 {} - 已处理 {}/{} 条记录，当前批次处理 {} 条",
                        threadName, industryName, totalProcessed, totalCount, batchProcessed);
                    
                    // 批次间延迟
                    if (i + batchSize < pks.size()) {
                        Thread.sleep((long) (delaySeconds * 1000));
                    }
                }
                
                currentPage++;
                
                // 页间延迟
                if (totalProcessed < totalCount) {
                    Thread.sleep(1000);
                }
            }
            
            log.info("[{}] 行业 {} 处理完成：成功 {} 个标准", 
                threadName, industryName, totalProcessed);
                
        } catch (Exception e) {
            log.error("[{}] 处理行业 {} 失败", threadName, industryName, e);
            totalFail.incrementAndGet();
        } finally {
            int completed = completedCategories.incrementAndGet();
            log.info("[{}] 总进度: {}/{} 个行业完成", 
                threadName, completed, getTotalCategoriesCount());
        }
    }
    
    /**
     * 获取总行业分类数量（用于进度显示）
     */
    private int getTotalCategoriesCount() {
        return detailInfoService.getIndustryCodesNeedingDetailInfo().size();
    }
}
