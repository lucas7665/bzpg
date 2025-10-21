package com.pig4cloud.pig.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pig4cloud.pig.admin.entity.LocalStandardDetail;

import java.util.List;

/**
 * 地方标准详情Service
 *
 * @author pig
 * @date 2025-10-15
 */
public interface LocalStandardDetailService extends IService<LocalStandardDetail> {

    /**
     * 获取需要爬取详细信息的标准PK列表（分页）
     */
    List<String> getPksNeedingDetailInfo(int offset, int limit);

    /**
     * 获取需要爬取详细信息的标准总数
     */
    long getCountNeedingDetailInfo();

    /**
     * 获取所有需要爬取详细信息的城市代码列表
     */
    List<String> getCityCodesNeedingDetailInfo();

    /**
     * 按城市获取需要爬取详细信息的标准PK列表（分页）
     */
    List<String> getPksNeedingDetailInfoByCity(String cityCode, int offset, int limit);

    /**
     * 按城市获取需要爬取详细信息的标准总数
     */
    long getCountNeedingDetailInfoByCity(String cityCode);

    /**
     * 获取需要下载文档的标准PK列表（分页）
     */
    List<String> getPksNeedingDownload(int offset, int limit);

    /**
     * 获取需要下载文档的标准总数
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
}
