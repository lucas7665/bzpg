package com.pig4cloud.pig.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.admin.entity.LocalStandardDetail;
import com.pig4cloud.pig.admin.mapper.LocalStandardDetailMapper;
import com.pig4cloud.pig.admin.service.LocalStandardQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 地方标准查询Service实现
 *
 * @author pig
 * @date 2025-10-21
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocalStandardQueryServiceImpl extends ServiceImpl<LocalStandardDetailMapper, LocalStandardDetail> 
        implements LocalStandardQueryService {

    @Override
    public List<LocalStandardDetail> getStandardsByProvince(String provinceName) {
        log.info("查询省份 {} 的标准列表", provinceName);
        
        // 方法1：通过city字段查询
        LambdaQueryWrapper<LocalStandardDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(LocalStandardDetail::getCity, provinceName)
               .orderByDesc(LocalStandardDetail::getCreateTime);
        
        List<LocalStandardDetail> result = list(wrapper);
        log.info("省份 {} 找到 {} 条标准", provinceName, result.size());
        
        return result;
    }

    /**
     * 根据城市代码查询标准列表
     * @param cityCode 城市代码，如bjzjj
     * @return 标准列表
     */
    public List<LocalStandardDetail> getStandardsByCityCode(String cityCode) {
        log.info("查询城市代码 {} 的标准列表", cityCode);
        
        LambdaQueryWrapper<LocalStandardDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LocalStandardDetail::getCityCode, cityCode)
               .orderByDesc(LocalStandardDetail::getCreateTime);
        
        List<LocalStandardDetail> result = list(wrapper);
        log.info("城市代码 {} 找到 {} 条标准", cityCode, result.size());
        
        return result;
    }

    @Override
    public List<LocalStandardDetail> getStandardsByCityCategoryId(Long cityCategoryId) {
        log.info("查询城市分类ID {} 的标准列表", cityCategoryId);
        
        LambdaQueryWrapper<LocalStandardDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LocalStandardDetail::getCityCategoryId, cityCategoryId)
               .orderByDesc(LocalStandardDetail::getCreateTime);
        
        List<LocalStandardDetail> result = list(wrapper);
        log.info("城市分类ID {} 找到 {} 条标准", cityCategoryId, result.size());
        
        return result;
    }

    @Override
    public long countStandardsByProvince(String provinceName) {
        log.info("统计省份 {} 的标准数量", provinceName);
        
        LambdaQueryWrapper<LocalStandardDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(LocalStandardDetail::getCity, provinceName);
        
        long count = count(wrapper);
        log.info("省份 {} 共有 {} 条标准", provinceName, count);
        
        return count;
    }

    @Override
    public ProvinceStandardStats getProvinceStandardStats(String provinceName) {
        log.info("获取省份 {} 的标准统计信息", provinceName);
        
        ProvinceStandardStats stats = new ProvinceStandardStats();
        stats.setProvinceName(provinceName);
        
        // 总数量
        LambdaQueryWrapper<LocalStandardDetail> totalWrapper = new LambdaQueryWrapper<>();
        totalWrapper.like(LocalStandardDetail::getCity, provinceName);
        stats.setTotalCount(count(totalWrapper));
        
        // 现行标准数量
        LambdaQueryWrapper<LocalStandardDetail> activeWrapper = new LambdaQueryWrapper<>();
        activeWrapper.like(LocalStandardDetail::getCity, provinceName)
                    .eq(LocalStandardDetail::getStatus, "现行");
        stats.setActiveCount(count(activeWrapper));
        
        // 废止标准数量
        LambdaQueryWrapper<LocalStandardDetail> abolishedWrapper = new LambdaQueryWrapper<>();
        abolishedWrapper.like(LocalStandardDetail::getCity, provinceName)
                       .eq(LocalStandardDetail::getStatus, "废止");
        stats.setAbolishedCount(count(abolishedWrapper));
        
        log.info("省份 {} 统计结果：总数={}, 现行={}, 废止={}", 
                provinceName, stats.getTotalCount(), stats.getActiveCount(), stats.getAbolishedCount());
        
        return stats;
    }
}
