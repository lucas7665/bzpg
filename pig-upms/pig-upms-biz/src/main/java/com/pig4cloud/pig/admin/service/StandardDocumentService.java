package com.pig4cloud.pig.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pig4cloud.pig.admin.entity.StandardDocument;

import java.util.List;

/**
 * 标准文档下载记录Service
 *
 * @author pig
 * @date 2025-10-15
 */
public interface StandardDocumentService extends IService<StandardDocument> {

    /**
     * 获取需要下载的标准PK列表（分页）
     */
    List<String> getPksNeedingDownload(int offset, int limit);

    /**
     * 获取需要下载的标准总数
     */
    long getCountNeedingDownload();

    /**
     * 获取需要重试下载的标准PK列表（分页）
     */
    List<String> getPksNeedingRetry(int offset, int limit, int maxRetries);

       /**
     * 获取需要重试下载的标准总数
     */
    long getCountNeedingRetry(int maxRetries);

    /**
     * 更新下载状态
     */
    void updateDownloadStatus(String pk, String status, String filePath, String captchaText, String downloadToken, String errorMessage);

    /**
     * 增加重试次数
     */
    void incrementRetryCount(String pk);
}
