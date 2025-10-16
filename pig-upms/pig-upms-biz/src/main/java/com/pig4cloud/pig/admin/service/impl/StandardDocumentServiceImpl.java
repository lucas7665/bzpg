package com.pig4cloud.pig.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.admin.entity.StandardDocument;
import com.pig4cloud.pig.admin.mapper.StandardDocumentMapper;
import com.pig4cloud.pig.admin.service.StandardDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 标准文档下载记录Service实现
 *
 * @author pig
 * @date 2025-10-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StandardDocumentServiceImpl extends ServiceImpl<StandardDocumentMapper, StandardDocument> implements StandardDocumentService {

    @Override
    public List<String> getPksNeedingDownload(int offset, int limit) {
        return baseMapper.getPksNeedingDownload(offset, limit);
    }

    @Override
    public long getCountNeedingDownload() {
        return baseMapper.getCountNeedingDownload();
    }

    @Override
    public List<String> getPksNeedingRetry(int offset, int limit, int maxRetries) {
        return baseMapper.getPksNeedingRetry(offset, limit, maxRetries);
    }

    @Override
    public long getCountNeedingRetry(int maxRetries) {
        return baseMapper.getCountNeedingRetry(maxRetries);
    }

    @Override
    public void updateDownloadStatus(String pk, String status, String filePath, String captchaText, String downloadToken, String errorMessage) {
        StandardDocument document = new StandardDocument();
        document.setPk(pk);
        document.setDownloadStatus(status);
        document.setFilePath(filePath);
        document.setCaptchaText(captchaText);
        document.setDownloadToken(downloadToken);
        document.setErrorMessage(errorMessage);
        document.setUpdateBy("system");
        document.setUpdateTime(LocalDateTime.now());

        if (StandardDocument.DownloadStatus.SUCCESS.getCode().equals(status)) {
            document.setDownloadTime(LocalDateTime.now());
        }

        // 先查询是否存在记录
        StandardDocument existing = getOne(new LambdaQueryWrapper<StandardDocument>()
                .eq(StandardDocument::getPk, pk));

        if (existing != null) {
            // 更新现有记录
            document.setId(existing.getId());
            document.setCreateBy(existing.getCreateBy());
            document.setCreateTime(existing.getCreateTime());
            updateById(document);
        } else {
            // 创建新记录
            document.setCreateBy("system");
            document.setCreateTime(LocalDateTime.now());
            save(document);
        }
    }

    @Override
    public void incrementRetryCount(String pk) {
        StandardDocument document = getOne(new LambdaQueryWrapper<StandardDocument>()
                .eq(StandardDocument::getPk, pk));

        if (document != null) {
            document.setRetryCount(document.getRetryCount() + 1);
            document.setUpdateBy("system");
            document.setUpdateTime(LocalDateTime.now());
            updateById(document);
        }
    }
}
