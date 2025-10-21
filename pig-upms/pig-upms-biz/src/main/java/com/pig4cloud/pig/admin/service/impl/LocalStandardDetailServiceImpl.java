package com.pig4cloud.pig.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.admin.entity.LocalStandardDetail;
import com.pig4cloud.pig.admin.mapper.LocalStandardDetailMapper;
import com.pig4cloud.pig.admin.service.LocalStandardDetailService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 地方标准详情Service实现
 *
 * @author pig
 * @date 2025-10-15
 */
@Service
public class LocalStandardDetailServiceImpl extends ServiceImpl<LocalStandardDetailMapper, LocalStandardDetail> implements LocalStandardDetailService {

    @Override
    public List<String> getPksNeedingDetailInfo(int offset, int limit) {
        return baseMapper.getPksNeedingDetailInfo(offset, limit);
    }

    @Override
    public long getCountNeedingDetailInfo() {
        return baseMapper.getCountNeedingDetailInfo();
    }

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
}
