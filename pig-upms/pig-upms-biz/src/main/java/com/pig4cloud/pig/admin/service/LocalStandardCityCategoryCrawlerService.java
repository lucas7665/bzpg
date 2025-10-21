package com.pig4cloud.pig.admin.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.pig4cloud.pig.admin.entity.LocalStandardCityCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 地方标准城市分类爬取服务
 *
 * @author pig
 * @date 2025-10-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocalStandardCityCategoryCrawlerService {

    private static final String CITY_LIST_URL = "https://dbba.sacinfo.org.cn/stdList";

    /**
     * 爬取城市分类列表
     */
    public List<LocalStandardCityCategory> crawlCityList() {
        log.info("开始爬取地方标准城市分类列表");
        
        try {
            // 1. 获取页面HTML
            String html = HttpUtil.get(CITY_LIST_URL);
            if (StrUtil.isBlank(html)) {
                log.error("获取城市列表页面失败");
                return new ArrayList<>();
            }

            // 2. 解析HTML
            Document doc = Jsoup.parse(html);
            
            // 3. 从JavaScript中提取城市数据
            List<CityData> cityDataList = extractCityDataFromScript(doc);
            log.info("从页面解析到 {} 个城市数据", cityDataList.size());

            // 4. 转换为实体对象
            List<LocalStandardCityCategory> cityCategories = new ArrayList<>();
            for (CityData cityData : cityDataList) {
                LocalStandardCityCategory category = convertToEntity(cityData);
                cityCategories.add(category);
            }

            log.info("成功解析 {} 个城市分类", cityCategories.size());
            return cityCategories;

        } catch (Exception e) {
            log.error("爬取城市分类列表失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 从JavaScript中提取城市数据
     */
    private List<CityData> extractCityDataFromScript(Document doc) {
        List<CityData> cityDataList = new ArrayList<>();
        
        try {
            // 查找包含城市数据的script标签
            Elements scripts = doc.select("script");
            for (Element script : scripts) {
                String content = script.html();
                if (content.contains("searchBoxs") && content.contains("ministry")) {
                    // 提取JSON数据
                    String jsonData = extractJsonFromScript(content);
                    if (StrUtil.isNotBlank(jsonData)) {
                        cityDataList = parseCityData(jsonData);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.error("从JavaScript提取城市数据失败", e);
        }
        
        return cityDataList;
    }

    /**
     * 从JavaScript中提取JSON数据
     */
    private String extractJsonFromScript(String scriptContent) {
        try {
            // 使用正则表达式提取data数组
            Pattern pattern = Pattern.compile("\"data\"\\s*:\\s*\\[(.*?)\\]", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(scriptContent);
            
            if (matcher.find()) {
                String jsonArray = "[" + matcher.group(1) + "]";
                return jsonArray;
            }
        } catch (Exception e) {
            log.error("提取JSON数据失败", e);
        }
        
        return null;
    }

    /**
     * 解析城市数据JSON
     */
    private List<CityData> parseCityData(String jsonData) {
        List<CityData> cityDataList = new ArrayList<>();
        
        try {
            JSONArray jsonArray = JSONUtil.parseArray(jsonData);
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                CityData cityData = new CityData();
                cityData.setValue(jsonObject.getStr("value"));
                cityData.setText(jsonObject.getStr("text"));
                cityDataList.add(cityData);
            }
        } catch (Exception e) {
            log.error("解析城市数据JSON失败", e);
        }
        
        return cityDataList;
    }

    /**
     * 转换为实体对象
     */
    private LocalStandardCityCategory convertToEntity(CityData cityData) {
        LocalStandardCityCategory category = new LocalStandardCityCategory();
        
        // 设置城市代码
        category.setCityCode(cityData.getValue());
        
        // 解析城市名称和数量
        String text = cityData.getText();
        if (StrUtil.isNotBlank(text)) {
            // 提取城市名称和数量，如"北京市(2,374)"
            Pattern pattern = Pattern.compile("(.+?)\\((\\d+(?:,\\d+)*)\\)");
            Matcher matcher = pattern.matcher(text);
            
            if (matcher.find()) {
                String cityName = matcher.group(1).trim();
                String countStr = matcher.group(2).replace(",", "");
                int count = Integer.parseInt(countStr);
                
                category.setCityName(cityName);
                category.setStandardCount(count);
                category.setTitle(text);
            } else {
                // 如果没有数量信息，只设置城市名称
                category.setCityName(text);
                category.setStandardCount(0);
                category.setTitle(text);
            }
        }
        
        // 设置其他字段
        category.setDataTrade(cityData.getValue());
        category.setCreateBy("system");
        category.setRemark("地方标准城市分类");
        
        return category;
    }

    /**
     * 城市数据内部类
     */
    private static class CityData {
        private String value;
        private String text;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}
