package com.pig4cloud.pig.admin.task;

import com.pig4cloud.pig.admin.entity.LocalStandardCityCategory;
import com.pig4cloud.pig.admin.entity.LocalStandardDetail;
import com.pig4cloud.pig.admin.service.LocalStandardCityCategoryCrawlerService;
import com.pig4cloud.pig.admin.service.LocalStandardDetailCrawlerService;
import com.pig4cloud.pig.admin.service.LocalStandardCityCategoryService;
import com.pig4cloud.pig.admin.service.LocalStandardDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 地方标准爬取定时任务
 *
 * @author pig
 * @date 2025-10-15
 */
@Slf4j
@Component("LocalStandardCrawler")
@RequiredArgsConstructor
public class LocalStandardCrawlerTask {

    private final LocalStandardCityCategoryCrawlerService cityCrawlerService;
    private final LocalStandardDetailCrawlerService detailCrawlerService;
    private final LocalStandardCityCategoryService cityCategoryService;
    private final LocalStandardDetailService detailService;

    @Value("${local-standard.crawl.batch-size:50}")
    private int batchSize;

    @Value("${local-standard.crawl.delay-seconds:2}")
    private int delaySeconds;

    /**
     * 爬取所有地方标准数据
     */
    public String crawlLocalStandards() {
        log.info("开始执行地方标准数据爬取任务");
        
        try {
            // 1. 爬取城市分类并保存到数据库
            log.info("第一阶段：开始爬取城市分类数据...");
            List<LocalStandardCityCategory> cities = cityCrawlerService.crawlCityList();
            log.info("成功解析 {} 个城市分类", cities.size());
            
            if (cities.isEmpty()) {
                return "城市分类数据为空，爬取终止";
            }
            
            // 保存城市分类到数据库
            cityCategoryService.saveBatch(cities);
            log.info("第一阶段完成：已保存 {} 个城市分类到数据库", cities.size());
            
            // 2. 从数据库获取城市列表，爬取各城市的标准详情
            log.info("第二阶段：开始爬取各城市标准详情...");
            List<LocalStandardCityCategory> savedCities = cityCategoryService.list();
            log.info("从数据库获取到 {} 个城市", savedCities.size());
            
            int totalStandards = 0;
            int successCount = 0;
            int failCount = 0;
            
            for (int i = 0; i < savedCities.size(); i++) {
                LocalStandardCityCategory city = savedCities.get(i);
                try {
                    log.info("开始爬取城市 {} 的标准数据...", city.getCityName());
                    
                    List<LocalStandardDetail> standards = detailCrawlerService
                        .crawlStandardDetailsByCity(city.getCityCode());
                    
                    if (standards != null && !standards.isEmpty()) {
                        // 保存标准详情到数据库
                        detailService.saveBatch(standards);
                        log.info("城市 {} 爬取完成，共 {} 条标准，已保存到数据库", 
                            city.getCityName(), standards.size());
                        totalStandards += standards.size();
                        successCount++;
                    } else {
                        log.warn("城市 {} 没有爬取到标准数据", city.getCityName());
                        failCount++;
                    }
                    
                    // 请求间隔
                    if (delaySeconds > 0) {
                        Thread.sleep(delaySeconds * 1000);
                    }
                    
                    // 每处理10个城市输出一次进度
                    if ((i + 1) % 10 == 0) {
                        log.info("已处理 {}/{} 个城市，成功: {}, 失败: {}", 
                            i + 1, savedCities.size(), successCount, failCount);
                    }
                    
                } catch (Exception e) {
                    log.error("爬取城市 {} 失败", city.getCityName(), e);
                    failCount++;
                }
            }
            
            String result = String.format("地方标准爬取完成：%d个城市，%d条标准，成功: %d, 失败: %d", 
                savedCities.size(), totalStandards, successCount, failCount);
            log.info(result);
            return result;
            
        } catch (Exception e) {
            log.error("执行地方标准数据爬取任务失败", e);
            return "地方标准爬取失败: " + e.getMessage();
        }
    }
}
