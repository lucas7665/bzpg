package com.pig4cloud.pig.admin.controller;

import com.pig4cloud.pig.admin.task.LocalStandardCrawlerTask;
import com.pig4cloud.pig.admin.task.LocalStandardDetailInfoCrawlerTask;
import com.pig4cloud.pig.admin.task.LocalStandardDocumentDownloadTask;
import com.pig4cloud.pig.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 地方标准爬取控制器
 *
 * @author pig
 * @date 2025-10-15
 */
@Slf4j
@RestController
@RequestMapping("/admin/local-standard")
@RequiredArgsConstructor
@Tag(name = "地方标准爬取", description = "地方标准爬取相关接口")
public class LocalStandardCrawlerController {

    private final LocalStandardCrawlerTask crawlerTask;
    private final LocalStandardDetailInfoCrawlerTask detailInfoCrawlerTask;
    private final LocalStandardDocumentDownloadTask documentDownloadTask;

    @PostMapping("/crawl")
    @Operation(summary = "开始爬取地方标准数据", description = "批量爬取所有地方标准数据")
    public R<String> crawlLocalStandards() {
        try {
            log.info("收到地方标准数据爬取请求");
            String result = crawlerTask.crawlLocalStandards();
            return R.ok(result);
        } catch (Exception e) {
            log.error("执行地方标准数据爬取失败", e);
            return R.failed("爬取任务执行失败: " + e.getMessage());
        }
    }

    @PostMapping("/crawl-detail-info")
    @Operation(summary = "开始爬取地方标准详细信息", description = "批量爬取地方标准详细信息")
    public R<String> crawlLocalStandardDetailInfos() {
        try {
            log.info("收到地方标准详细信息爬取请求");
            String result = detailInfoCrawlerTask.crawlLocalStandardDetailInfos();
            return R.ok(result);
        } catch (Exception e) {
            log.error("执行地方标准详细信息爬取失败", e);
            return R.failed("详细信息爬取任务执行失败: " + e.getMessage());
        }
    }

    @PostMapping("/download")
    @Operation(summary = "开始下载地方标准文档", description = "批量下载地方标准PDF文档")
    public R<String> downloadLocalStandardDocuments() {
        try {
            log.info("收到地方标准文档下载请求");
            String result = documentDownloadTask.downloadAllDocuments();
            return R.ok(result);
        } catch (Exception e) {
            log.error("执行地方标准文档下载失败", e);
            return R.failed("文档下载任务执行失败: " + e.getMessage());
        }
    }

    @PostMapping("/retry-download")
    @Operation(summary = "重试失败的地方标准下载任务", description = "重试所有下载失败的地方标准文档")
    public R<String> retryFailedDownloads() {
        try {
            log.info("收到地方标准失败下载任务重试请求");
            String result = documentDownloadTask.retryFailedDownloads();
            return R.ok(result);
        } catch (Exception e) {
            log.error("执行地方标准失败下载任务重试失败", e);
            return R.failed("重试任务执行失败: " + e.getMessage());
        }
    }
}
