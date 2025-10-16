package com.pig4cloud.pig.admin.controller;

import com.pig4cloud.pig.admin.task.StandardDocumentDownloadTask;
import com.pig4cloud.pig.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 标准文档下载控制器
 *
 * @author pig
 * @date 2025-10-15
 */
@Slf4j
@RestController
@RequestMapping("/admin/standard-document")
@RequiredArgsConstructor
@Tag(name = "标准文档下载", description = "标准文档下载相关接口")
public class StandardDocumentDownloadController {

    private final StandardDocumentDownloadTask downloadTask;

    @PostMapping("/download")
    @Operation(summary = "开始下载所有标准文档", description = "批量下载所有待下载的标准文档")
    public R<String> downloadAllDocuments() {
        try {
            log.info("收到标准文档下载请求");
            String result = downloadTask.downloadAllDocuments();
            return R.ok(result);
        } catch (Exception e) {
            log.error("执行标准文档下载失败", e);
            return R.failed("下载任务执行失败: " + e.getMessage());
        }
    }

    @PostMapping("/retry")
    @Operation(summary = "重试失败的下载任务", description = "重试所有下载失败的标准文档")
    public R<String> retryFailedDownloads() {
        try {
            log.info("收到失败下载任务重试请求");
            String result = downloadTask.retryFailedDownloads();
            return R.ok(result);
        } catch (Exception e) {
            log.error("执行失败下载任务重试失败", e);
            return R.failed("重试任务执行失败: " + e.getMessage());
        }
    }
}
