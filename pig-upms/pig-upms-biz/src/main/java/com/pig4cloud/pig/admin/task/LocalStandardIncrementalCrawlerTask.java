package com.pig4cloud.pig.admin.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pig4cloud.pig.admin.entity.LocalStandardCityCategory;
import com.pig4cloud.pig.admin.entity.LocalStandardDetail;
import com.pig4cloud.pig.admin.entity.LocalStandardDetailInfo;
import com.pig4cloud.pig.admin.mapper.LocalStandardDetailInfoMapper;
import com.pig4cloud.pig.admin.mapper.LocalStandardDetailMapper;
import com.pig4cloud.pig.admin.service.LocalStandardCityCategoryService;
import com.pig4cloud.pig.admin.service.LocalStandardDetailCrawlerService;
import com.pig4cloud.pig.admin.service.LocalStandardDetailInfoCrawlerService;
import com.pig4cloud.pig.admin.service.LocalStandardDetailInfoService;
import com.pig4cloud.pig.admin.service.LocalStandardDetailService;
import com.pig4cloud.pig.admin.service.LocalStandardDocumentDownloadService;
import com.pig4cloud.pig.admin.mapper.LocalStandardDocumentMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 地方标准增量爬取定时任务（最近一个月）
 *
 * @author pig
 * @date 2025-10-15
 */
@Slf4j
@Component("LocalStandardIncrementalCrawler")
@RequiredArgsConstructor
public class LocalStandardIncrementalCrawlerTask {

    private final LocalStandardDetailCrawlerService detailCrawlerService;
    private final LocalStandardDetailService detailService;
    private final LocalStandardDetailMapper detailMapper;
    private final LocalStandardCityCategoryService cityCategoryService;
    private final LocalStandardDetailInfoCrawlerService detailInfoCrawlerService;
    private final LocalStandardDetailInfoService detailInfoService;
    private final LocalStandardDetailInfoMapper detailInfoMapper;
    private final LocalStandardDocumentDownloadService documentDownloadService;
    private final LocalStandardDocumentMapper documentMapper;

    /**
     * 增量爬取地方标准数据（最近一个月）
     * @return 执行结果
     */
    @SneakyThrows
    public String crawlIncrementalLocalStandards() {
        log.info("开始执行地方标准增量爬取任务（最近一个月）");

        try {
            // 第一步：使用 pubdate=-1 获取所有城市最近一个月的数据
            log.info("开始爬取所有城市最近一个月的地方标准数据...");

            List<LocalStandardDetail> allStandards = detailCrawlerService.crawlIncrementalStandardDetails();

            if (allStandards.isEmpty()) {
                log.info("最近一个月没有新的地方标准数据");
                return "0"; // 成功，但没有数据
            }

            log.info("爬取到 {} 条最近一个月的地方标准数据", allStandards.size());

            // 第二步：数据去重（基于 pk）
            // 因为在同一批次中可能有重复的 pk，需要先去重
            Map<String, LocalStandardDetail> uniqueStandardsMap = new LinkedHashMap<>();
            for (LocalStandardDetail standard : allStandards) {
                String pk = standard.getPk();
                if (pk != null && !pk.isEmpty()) {
                    // 如果已存在，保留最新的（后爬取的覆盖先爬取的）
                    uniqueStandardsMap.put(pk, standard);
                }
            }
            List<LocalStandardDetail> uniqueStandards = new ArrayList<>(uniqueStandardsMap.values());
            log.info("去重后剩余 {} 条记录（去重前 {} 条）", uniqueStandards.size(), allStandards.size());

            if (uniqueStandards.isEmpty()) {
                log.info("去重后没有有效数据");
                return "0"; // 成功，但没有有效数据
            }

            // 第三步：处理城市分类关联
            if (!uniqueStandards.isEmpty()) {
                log.info("开始处理 {} 条数据的城市分类关联...", uniqueStandards.size());

                // 方法1：从数据库查询 city 和 city_code 的对应关系
                Map<String, String> cityNameToCodeMap = buildCityNameToCodeMap();
                log.info("从数据库获取到 {} 个城市名称与代码的对应关系", cityNameToCodeMap.size());

                // 方法2：获取所有城市分类用于关联（作为备用）
                List<LocalStandardCityCategory> categories = cityCategoryService.list();
                Map<String, LocalStandardCityCategory> categoryMap = categories.stream()
                    .collect(Collectors.toMap(LocalStandardCityCategory::getCityCode, Function.identity(), (v1, v2) -> v1));

                // 设置每条数据的城市分类关联
                int matchedCount = 0;
                int unmatchedCount = 0;
                for (LocalStandardDetail standard : uniqueStandards) {
                    try {
                        // 从 city 字段获取城市名称
                        String cityName = standard.getCity();
                        if (cityName != null && !cityName.isEmpty()) {
                            // 优先方法1：从数据库查询的对应关系获取 city_code
                            String cityCode = cityNameToCodeMap.get(cityName);
                            
                            if (cityCode != null) {
                                // 找到对应的 city_code，设置关联
                                standard.setCityCode(cityCode);
                                LocalStandardCityCategory category = categoryMap.get(cityCode);
                                if (category != null) {
                                    standard.setCityCategoryId(category.getId());
                                    matchedCount++;
                                    log.debug("标准 {} 关联城市分类: {} ({})", 
                                        standard.getPk(), cityName, cityCode);
                                } else {
                                    log.warn("找到城市代码 {} 但未找到对应的分类记录", cityCode);
                                    unmatchedCount++;
                                }
                            } else {
                                // 方法2：尝试从城市分类表中匹配（通过城市名称）
                                LocalStandardCityCategory category = categories.stream()
                                    .filter(c -> cityName.equals(c.getCityName()))
                                    .findFirst()
                                    .orElse(null);

                                if (category != null) {
                                    standard.setCityCategoryId(category.getId());
                                    standard.setCityCode(category.getCityCode());
                                    matchedCount++;
                                    log.debug("标准 {} 通过城市分类表关联: {} ({})", 
                                        standard.getPk(), category.getCityName(), category.getCityCode());
                                } else {
                                    log.warn("未找到城市名称 {} 对应的分类", cityName);
                                    unmatchedCount++;
                                }
                            }
                        } else {
                            log.debug("标准 {} 的城市名称为空", standard.getPk());
                            unmatchedCount++;
                        }

                        standard.setCreateBy("system");
                        standard.setUpdateBy("system");

                    } catch (Exception e) {
                        log.warn("处理标准 {} 的城市分类失败", standard.getPk(), e);
                        unmatchedCount++;
                    }
                }

                log.info("城市分类关联完成：匹配 {} 条，未匹配 {} 条", matchedCount, unmatchedCount);

                // 批量保存：先查询增量数据中哪些在数据库中存在，然后分离处理
                // 收集所有 pk
                List<String> allPks = uniqueStandards.stream()
                    .map(LocalStandardDetail::getPk)
                    .filter(pk -> pk != null && !pk.isEmpty())
                    .collect(Collectors.toList());

                // 查询数据库中已存在的记录（同时获取 id 和 pk）
                Map<String, Long> existingPkToIdMap = new HashMap<>();
                if (!allPks.isEmpty()) {
                    // 分批查询，避免 IN 子句过长
                    int queryBatchSize = 1000;
                    for (int i = 0; i < allPks.size(); i += queryBatchSize) {
                        int end = Math.min(i + queryBatchSize, allPks.size());
                        List<String> batchPks = allPks.subList(i, end);
                        // 使用 new LambdaQueryWrapper 查询（与其他地方保持一致）
                        List<LocalStandardDetail> existing = detailService.list(
                            new LambdaQueryWrapper<LocalStandardDetail>()
                                .in(LocalStandardDetail::getPk, batchPks)
                        );
                        // 构建 pk -> id 的映射
                        for (LocalStandardDetail existingDetail : existing) {
                            if (existingDetail.getPk() != null && existingDetail.getId() != null) {
                                existingPkToIdMap.put(existingDetail.getPk(), existingDetail.getId());
                            }
                        }
                    }
                    log.info("查询到数据库中已存在 {} 条记录", existingPkToIdMap.size());
                }

                // 分离需要插入和更新的数据
                List<LocalStandardDetail> toInsert = new ArrayList<>();
                List<LocalStandardDetail> toUpdate = new ArrayList<>();

                for (LocalStandardDetail standard : uniqueStandards) {
                    String pk = standard.getPk();
                    if (pk != null && !pk.isEmpty()) {
                        Long existingId = existingPkToIdMap.get(pk);
                        if (existingId != null) {
                            // 已存在，设置 id 用于更新
                            standard.setId(existingId);
                            toUpdate.add(standard);
                        } else {
                            // 不存在，需要插入
                            toInsert.add(standard);
                        }
                    }
                }

                log.info("需要插入 {} 条，需要更新 {} 条", toInsert.size(), toUpdate.size());

                // 分批插入新数据
                int insertCount = 0;
                if (!toInsert.isEmpty()) {
                    int insertBatchSize = 500;
                    for (int i = 0; i < toInsert.size(); i += insertBatchSize) {
                        int end = Math.min(i + insertBatchSize, toInsert.size());
                        List<LocalStandardDetail> batch = toInsert.subList(i, end);
                        try {
                            detailService.saveBatch(batch);
                            insertCount += batch.size();
                            log.info("已插入 {}/{} 条新记录", insertCount, toInsert.size());
                        } catch (Exception e) {
                            log.error("批量插入失败，降级为逐条插入", e);
                            for (LocalStandardDetail standard : batch) {
                                try {
                                    detailService.save(standard);
                                    insertCount++;
                                } catch (Exception ex) {
                                    log.error("插入标准 {} 失败", standard.getPk(), ex);
                                }
                            }
                        }
                    }
                }

                // 分批更新已存在的数据
                int updateCount = 0;
                if (!toUpdate.isEmpty()) {
                    int updateBatchSize = 500;
                    for (int i = 0; i < toUpdate.size(); i += updateBatchSize) {
                        int end = Math.min(i + updateBatchSize, toUpdate.size());
                        List<LocalStandardDetail> batch = toUpdate.subList(i, end);
                        try {
                            detailService.updateBatchById(batch);
                            updateCount += batch.size();
                            log.info("已更新 {}/{} 条记录", updateCount, toUpdate.size());
                        } catch (Exception e) {
                            log.error("批量更新失败，降级为逐条更新", e);
                            for (LocalStandardDetail standard : batch) {
                                try {
                                    detailService.updateById(standard);
                                    updateCount++;
                                } catch (Exception ex) {
                                    log.error("更新标准 {} 失败", standard.getPk(), ex);
                                }
                            }
                        }
                    }
                }

                log.info("标准数据保存完成：插入 {} 条，更新 {} 条，总计 {} 条", 
                    insertCount, updateCount, insertCount + updateCount);
            }

            // 第四步：可选 - 爬取这批数据的详细信息
            if (uniqueStandards != null && !uniqueStandards.isEmpty()) {
                log.info("开始爬取增量数据的详细信息...");

                // 收集所有pk
                List<String> pks = uniqueStandards.stream()
                    .map(LocalStandardDetail::getPk)
                    .filter(pk -> pk != null && !pk.isEmpty())
                    .collect(Collectors.toList());

                log.info("需要爬取详细信息的标准数量：{}", pks.size());

                // 批量爬取详细信息（每次处理100条）
                int batchSize = 100;
                int totalProcessed = 0;
                int successCount = 0;
                int failCount = 0;
                int totalInsertedInfo = 0;  // 总插入数量
                int totalUpdatedInfo = 0;   // 总更新数量

                for (int i = 0; i < pks.size(); i += batchSize) {
                    int end = Math.min(i + batchSize, pks.size());
                    List<String> batch = pks.subList(i, end);

                    try {
                        // 根据 pk 获取标准详情（使用 new LambdaQueryWrapper，与其他地方保持一致）
                        List<LocalStandardDetail> batchDetails = detailService.list(
                            new LambdaQueryWrapper<LocalStandardDetail>()
                                .in(LocalStandardDetail::getPk, batch)
                        );

                        // 收集爬取到的详细信息
                        List<LocalStandardDetailInfo> batchInfoList = new ArrayList<>();

                        // 爬取这批详细数据
                        for (LocalStandardDetail detail : batchDetails) {
                            try {
                                LocalStandardDetailInfo info = detailInfoCrawlerService.crawlStandardDetailInfo(detail);
                                if (info != null) {
                                    // 确保 pk 已设置
                                    if (info.getPk() == null) {
                                        info.setPk(detail.getPk());
                                    }
                                    batchInfoList.add(info);
                                    successCount++;
                                } else {
                                    log.warn("标准 {} 详细信息爬取返回 null", detail.getPk());
                                    failCount++;
                                }
                                
                                // 单条记录间添加延迟
                                Thread.sleep(100);
                            } catch (Exception e) {
                                log.error("处理标准 {} 详细信息失败", detail.getPk(), e);
                                failCount++;
                            }
                        }

                        // 批量保存详细信息到数据库（先查询已存在的记录，分离插入和更新）
                        if (!batchInfoList.isEmpty()) {
                            // 收集所有 pk
                            List<String> infoPks = batchInfoList.stream()
                                .map(LocalStandardDetailInfo::getPk)
                                .filter(pk -> pk != null && !pk.isEmpty())
                                .collect(Collectors.toList());

                            // 查询数据库中已存在的记录（获取 pk -> id 映射）
                            Map<String, Long> existingInfoPkToIdMap = new HashMap<>();
                            if (!infoPks.isEmpty()) {
                                List<Map<String, Object>> mappings = detailInfoMapper.getPkToIdMapping(infoPks);
                                for (Map<String, Object> mapping : mappings) {
                                    String pk = (String) mapping.get("pk");
                                    Object idObj = mapping.get("id");
                                    if (pk != null && idObj != null) {
                                        Long id = idObj instanceof Long ? (Long) idObj : ((Number) idObj).longValue();
                                        existingInfoPkToIdMap.put(pk, id);
                                    }
                                }
                            }

                            // 分离需要插入和更新的数据
                            List<LocalStandardDetailInfo> toInsertInfo = new ArrayList<>();
                            List<LocalStandardDetailInfo> toUpdateInfo = new ArrayList<>();

                            // 当前时间，用于更新操作
                            LocalDateTime now = LocalDateTime.now();
                            
                            for (LocalStandardDetailInfo info : batchInfoList) {
                                String pk = info.getPk();
                                if (pk != null && !pk.isEmpty()) {
                                    Long existingId = existingInfoPkToIdMap.get(pk);
                                    if (existingId != null) {
                                        // 已存在，设置 id 用于更新
                                        info.setId(existingId);
                                        // 手动设置更新时间和更新人，确保 update_time 字段会被更新
                                        info.setUpdateTime(now);
                                        info.setUpdateBy("system");
                                        toUpdateInfo.add(info);
                                    } else {
                                        // 不存在，需要插入
                                        toInsertInfo.add(info);
                                    }
                                }
                            }

                            // 分批插入新数据
                            if (!toInsertInfo.isEmpty()) {
                                try {
                                    detailInfoService.saveBatch(toInsertInfo);
                                    totalInsertedInfo += toInsertInfo.size();
                                    log.info("批次 {} 详细信息插入成功，共 {} 条（本批次插入: {} 条，更新: {} 条）", 
                                        (i / batchSize + 1), batchInfoList.size(), toInsertInfo.size(), toUpdateInfo.size());
                                } catch (Exception e) {
                                    log.error("批次 {} 详细信息批量插入失败，降级为逐条插入", 
                                        (i / batchSize + 1), e);
                                    int inserted = 0;
                                    for (LocalStandardDetailInfo info : toInsertInfo) {
                                        try {
                                            detailInfoService.save(info);
                                            inserted++;
                                        } catch (Exception ex) {
                                            log.error("插入标准 {} 详细信息失败", info.getPk(), ex);
                                        }
                                    }
                                    totalInsertedInfo += inserted;
                                }
                            } else if (!toUpdateInfo.isEmpty()) {
                                // 只有更新，没有插入
                                log.info("批次 {} 详细信息全部为更新，共 {} 条", 
                                    (i / batchSize + 1), toUpdateInfo.size());
                            }

                            // 分批更新已存在的数据
                            if (!toUpdateInfo.isEmpty()) {
                                try {
                                    detailInfoService.updateBatchById(toUpdateInfo);
                                    totalUpdatedInfo += toUpdateInfo.size();
                                    log.debug("批次 {} 详细信息更新成功，共 {} 条", 
                                        (i / batchSize + 1), toUpdateInfo.size());
                                } catch (Exception e) {
                                    log.error("批次 {} 详细信息批量更新失败，降级为逐条更新", 
                                        (i / batchSize + 1), e);
                                    int updated = 0;
                                    for (LocalStandardDetailInfo info : toUpdateInfo) {
                                        try {
                                            detailInfoService.updateById(info);
                                            updated++;
                                        } catch (Exception ex) {
                                            log.error("更新标准 {} 详细信息失败", info.getPk(), ex);
                                        }
                                    }
                                    totalUpdatedInfo += updated;
                                }
                            }
                        }

                        totalProcessed += batch.size();

                        log.info("已处理详细信息 {}/{} 条记录，成功: {}, 失败: {}", 
                            totalProcessed, pks.size(), successCount, failCount);

                        // 批次间延迟
                        if (end < pks.size()) {
                            Thread.sleep(200);
                        }

                    } catch (Exception e) {
                        log.error("处理详细信息批次失败", e);
                        failCount += batch.size();
                    }
                }

                log.info("详细信息爬取完成，共处理 {} 条记录，成功: {}, 失败: {}", 
                    totalProcessed, successCount, failCount);
                log.info("详细信息保存完成：插入 {} 条，更新 {} 条，总计 {} 条", 
                    totalInsertedInfo, totalUpdatedInfo, totalInsertedInfo + totalUpdatedInfo);
            }

            // 第五步：下载增量数据的文档
            if (uniqueStandards != null && !uniqueStandards.isEmpty()) {
                log.info("开始下载增量数据的文档，共 {} 条标准", uniqueStandards.size());
                
                // 收集所有增量数据的 pk
                List<String> allPks = uniqueStandards.stream()
                    .map(LocalStandardDetail::getPk)
                    .filter(pk -> pk != null && !pk.isEmpty())
                    .collect(Collectors.toList());
                
                if (!allPks.isEmpty()) {
                    // 查询已成功下载的 pk
                    Set<String> downloadedPks = new HashSet<>();
                    try {
                        // 分批查询，避免 IN 子句过长
                        int batchSize = 500;
                        for (int i = 0; i < allPks.size(); i += batchSize) {
                            int end = Math.min(i + batchSize, allPks.size());
                            List<String> batchPks = allPks.subList(i, end);
                            List<String> batchDownloaded = documentMapper.getDownloadedPks(batchPks);
                            downloadedPks.addAll(batchDownloaded);
                        }
                        log.info("查询到 {} 个已下载的文档", downloadedPks.size());
                    } catch (Exception e) {
                        log.error("查询已下载文档列表失败", e);
                    }
                    
                    // 过滤出需要下载的 pk
                    List<String> pksToDownload = allPks.stream()
                        .filter(pk -> !downloadedPks.contains(pk))
                        .collect(Collectors.toList());
                    
                    log.info("需要下载的文档：{} 个（已下载: {} 个，待下载: {} 个）", 
                        allPks.size(), downloadedPks.size(), pksToDownload.size());
                    
                    if (!pksToDownload.isEmpty()) {
                        // 根据 pk 查询详情列表
                        List<LocalStandardDetail> detailsToDownload = detailService.list(
                            new LambdaQueryWrapper<LocalStandardDetail>()
                                .in(LocalStandardDetail::getPk, pksToDownload)
                        );
                        
                        log.info("开始下载 {} 个文档", detailsToDownload.size());
                        
                        int downloadSuccessCount = 0;
                        int downloadFailCount = 0;
                        int maxRetries = 3; // 最大重试次数
                        int delaySeconds = 2; // 下载间隔（秒）
                        
                        for (LocalStandardDetail detail : detailsToDownload) {
                            try {
                                if (documentDownloadService.downloadDocumentWithRetry(detail, maxRetries)) {
                                    downloadSuccessCount++;
                                    log.debug("文档下载成功: {}", detail.getPk());
                                } else {
                                    downloadFailCount++;
                                    log.warn("文档下载失败: {}", detail.getPk());
                                }
                                
                                // 下载间隔，避免请求过快
                                if (delaySeconds > 0) {
                                    Thread.sleep(delaySeconds * 1000);
                                }
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                log.warn("文档下载任务被中断");
                                break;
                            } catch (Exception e) {
                                downloadFailCount++;
                                log.error("下载文档异常: {}", detail.getPk(), e);
                            }
                        }
                        
                        log.info("文档下载完成：成功 {} 个，失败 {} 个，总计 {} 个", 
                            downloadSuccessCount, downloadFailCount, detailsToDownload.size());
                    } else {
                        log.info("所有文档都已下载，无需重新下载");
                    }
                }
            }

            log.info("增量爬取任务完成：共处理 {} 条标准数据", uniqueStandards != null ? uniqueStandards.size() : 0);

            return "0"; // 成功
        } catch (Exception e) {
            log.error("增量爬取地方标准数据失败", e);
            return "1"; // 失败
        }
    }

    /**
     * 增量爬取地方标准数据（带参数版本）
     * @param params 参数（可为空）
     * @return 执行结果
     */
    @SneakyThrows
    public String crawlIncrementalLocalStandards(String params) {
        log.info("开始执行地方标准增量爬取任务（最近一个月），参数：{}", params);

        try {
            String result = crawlIncrementalLocalStandards();

            if ("0".equals(result)) {
                log.info("地方标准增量爬取任务执行成功");
                return "0"; // 成功
            } else {
                log.error("地方标准增量爬取任务执行失败");
                return "1"; // 失败
            }
        } catch (Exception e) {
            log.error("地方标准增量爬取任务执行异常", e);
            return "1"; // 失败
        }
    }

    /**
     * 构建城市名称到城市代码的映射关系
     * 从数据库中查询 local_standard_detail 表中已有的 city 和 city_code 对应关系
     * @return 城市名称 -> 城市代码的映射
     */
    private Map<String, String> buildCityNameToCodeMap() {
        Map<String, String> cityNameToCodeMap = new HashMap<>();
        
        try {
            // 使用 Mapper 自定义查询方法获取城市名称和代码的对应关系
            List<Map<String, String>> mappings = detailMapper.getCityNameToCodeMapping();

            // 构建映射关系（如果有重复的城市名称，取第一个）
            for (Map<String, String> mapping : mappings) {
                String cityName = mapping.get("city");
                String cityCode = mapping.get("city_code");
                if (cityName != null && cityCode != null && 
                    !cityName.isEmpty() && !cityCode.isEmpty()) {
                    // 如果已存在，不覆盖（保持第一次出现的映射）
                    cityNameToCodeMap.putIfAbsent(cityName, cityCode);
                }
            }

            log.debug("从数据库构建城市映射关系：{} 个城市", cityNameToCodeMap.size());
        } catch (Exception e) {
            log.error("构建城市名称到代码映射关系失败", e);
        }

        return cityNameToCodeMap;
    }
}

