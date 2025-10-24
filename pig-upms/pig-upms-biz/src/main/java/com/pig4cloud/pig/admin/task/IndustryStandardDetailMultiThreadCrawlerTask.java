package com.pig4cloud.pig.admin.task;

import com.pig4cloud.pig.admin.service.IndustryStandardDetailMultiThreadCrawler;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 行业标准详细信息多线程爬取定时任务
 *
 * @author pig4cloud
 */
@Slf4j
@Component("IndustryStandardDetailMultiThreadCrawler")
@RequiredArgsConstructor
public class IndustryStandardDetailMultiThreadCrawlerTask {

    private final IndustryStandardDetailMultiThreadCrawler multiThreadCrawler;

    /**
     * 多线程爬取行业标准详细信息（无参数版本）
     * @return 执行结果
     */
    @SneakyThrows
    public String crawlStandardDetailsMultiThread() {
        log.info("开始执行行业标准详细信息多线程爬取任务");
        
        try {
            String result = multiThreadCrawler.crawlAllStandardDetailsMultiThread();
            
            if ("0".equals(result)) {
                log.info("行业标准详细信息多线程爬取任务执行成功");
                return "0"; // 成功
            } else {
                log.error("行业标准详细信息多线程爬取任务执行失败");
                return "1"; // 失败
            }
        } catch (Exception e) {
            log.error("行业标准详细信息多线程爬取任务执行异常", e);
            return "1"; // 失败
        }
    }

    /**
     * 多线程爬取行业标准详细信息（带参数版本）
     * @param params 参数（可为空）
     * @return 执行结果
     */
    @SneakyThrows
    public String crawlStandardDetailsMultiThread(String params) {
        log.info("开始执行行业标准详细信息多线程爬取任务，参数：{}", params);
        
        try {
            String result = multiThreadCrawler.crawlAllStandardDetailsMultiThread();
            
            if ("0".equals(result)) {
                log.info("行业标准详细信息多线程爬取任务执行成功");
                return "0"; // 成功
            } else {
                log.error("行业标准详细信息多线程爬取任务执行失败");
                return "1"; // 失败
            }
        } catch (Exception e) {
            log.error("行业标准详细信息多线程爬取任务执行异常", e);
            return "1"; // 失败
        }
    }
}
