package com.pig4cloud.pig.admin.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.pig4cloud.pig.admin.entity.LocalStandardDetail;
import com.pig4cloud.pig.admin.entity.LocalStandardDetailInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 地方标准详细信息爬取服务
 *
 * @author pig
 * @date 2025-10-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocalStandardDetailInfoCrawlerService {

    private static final String STD_DETAIL_URL = "https://dbba.sacinfo.org.cn/stdDetail/";

    /**
     * 爬取标准详细信息
     */
    public LocalStandardDetailInfo crawlStandardDetailInfo(LocalStandardDetail detail) {
        String pk = detail.getPk();
        log.debug("开始爬取标准 {} 的详细信息", pk);
        
        try {
            // 1. 获取详情页面
            String detailUrl = STD_DETAIL_URL + pk;
            String html = HttpUtil.get(detailUrl);
            
            if (StrUtil.isBlank(html)) {
                log.warn("标准 {} 详情页面为空", pk);
                return null;
            }
            
            // 2. 解析HTML
            Document doc = Jsoup.parse(html);
            
            // 3. 创建详细信息对象
            LocalStandardDetailInfo info = new LocalStandardDetailInfo();
            info.setPk(pk);
            info.setStandardCode(detail.getCode());
            info.setChName(detail.getChName());
            
            // 4. 解析各个信息段落
            parseStandardStatus(doc, info);
            parseBasicInfo(doc, info);
            parseRecordInfo(doc, info);
            parseScope(doc, info);
            parseDraftingInfo(doc, info);
            
            // 5. 设置系统字段
            info.setCreateBy("system");
            info.setRemark("地方标准详细信息");
            
            log.debug("标准 {} 详细信息爬取完成", pk);
            return info;
            
        } catch (Exception e) {
            log.error("爬取标准 {} 详细信息失败", pk, e);
            return null;
        }
    }

    /**
     * 解析标准状态
     */
    private void parseStandardStatus(Document doc, LocalStandardDetailInfo info) {
        try {
            // 查找标准状态段落
            Element statusSection = doc.select("div.para-title:contains(标准状态)").first();
            if (statusSection != null) {
                // 解析发布时间
                Element publishElement = doc.select("div.timeline a:contains(发布)").first();
                if (publishElement != null) {
                    String publishText = publishElement.text();
                    if (publishText.contains("于")) {
                        String publishDate = publishText.substring(publishText.indexOf("于") + 1).trim();
                        info.setPublishDate(publishDate);
                    }
                }
                
                // 解析实施时间
                Element implementElement = doc.select("div.timeline a:contains(实施)").first();
                if (implementElement != null) {
                    String implementText = implementElement.text();
                    if (implementText.contains("于")) {
                        String implementDate = implementText.substring(implementText.indexOf("于") + 1).trim();
                        info.setImplementDate(implementDate);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("解析标准状态失败", e);
        }
    }

    /**
     * 解析基础信息
     */
    private void parseBasicInfo(Document doc, LocalStandardDetailInfo info) {
        try {
            // 查找基础信息段落
            Element basicInfoSection = doc.select("div.para-title:contains(基础信息)").first();
            if (basicInfoSection != null) {
                // 解析制修订类型
                Element revisionElement = basicInfoSection.nextElementSibling();
                if (revisionElement != null) {
                    Element revisionTypeElement = revisionElement.select("dt:contains(制修订)").first();
                    if (revisionTypeElement != null) {
                        Element valueElement = revisionTypeElement.nextElementSibling();
                        if (valueElement != null) {
                            info.setRevisionType(valueElement.text().trim());
                        }
                    }
                }
                
                // 解析代替标准
                Element replaceElement = basicInfoSection.nextElementSibling();
                if (replaceElement != null) {
                    Element replaceTypeElement = replaceElement.select("dt:contains(代替标准)").first();
                    if (replaceTypeElement != null) {
                        Element valueElement = replaceTypeElement.nextElementSibling();
                        if (valueElement != null) {
                            info.setReplaceStandard(valueElement.text().trim());
                        }
                    }
                }
                
                // 解析中国标准分类号
                Element chinaElement = basicInfoSection.nextElementSibling();
                if (chinaElement != null) {
                    Element chinaTypeElement = chinaElement.select("dt:contains(中国标准分类号)").first();
                    if (chinaTypeElement != null) {
                        Element valueElement = chinaTypeElement.nextElementSibling();
                        if (valueElement != null) {
                            info.setChinaClassification(valueElement.text().trim());
                        }
                    }
                }
                
                // 解析国际标准分类号
                Element internationalElement = basicInfoSection.nextElementSibling();
                if (internationalElement != null) {
                    Element internationalTypeElement = internationalElement.select("dt:contains(国际标准分类号)").first();
                    if (internationalTypeElement != null) {
                        Element valueElement = internationalTypeElement.nextElementSibling();
                        if (valueElement != null) {
                            info.setInternationalClassification(valueElement.text().trim());
                        }
                    }
                }
                
                // 解析技术归口
                Element technicalElement = basicInfoSection.nextElementSibling();
                if (technicalElement != null) {
                    Element technicalTypeElement = technicalElement.select("dt:contains(技术归口)").first();
                    if (technicalTypeElement != null) {
                        Element valueElement = technicalTypeElement.nextElementSibling();
                        if (valueElement != null) {
                            info.setTechnicalCommittee(valueElement.text().trim());
                        }
                    }
                }
                
                // 解析批准发布部门
                Element approvalElement = basicInfoSection.nextElementSibling();
                if (approvalElement != null) {
                    Element approvalTypeElement = approvalElement.select("dt:contains(批准发布部门)").first();
                    if (approvalTypeElement != null) {
                        Element valueElement = approvalTypeElement.nextElementSibling();
                        if (valueElement != null) {
                            info.setApprovalDepartment(valueElement.text().trim());
                        }
                    }
                }
                
                // 解析行业分类
                Element industryElement = basicInfoSection.nextElementSibling();
                if (industryElement != null) {
                    Element industryTypeElement = industryElement.select("dt:contains(行业分类)").first();
                    if (industryTypeElement != null) {
                        Element valueElement = industryTypeElement.nextElementSibling();
                        if (valueElement != null) {
                            info.setCityClassification(valueElement.text().trim());
                        }
                    }
                }
                
                // 解析标准类别
                Element categoryElement = basicInfoSection.nextElementSibling();
                if (categoryElement != null) {
                    Element categoryTypeElement = categoryElement.select("dt:contains(标准类别)").first();
                    if (categoryTypeElement != null) {
                        Element valueElement = categoryTypeElement.nextElementSibling();
                        if (valueElement != null) {
                            info.setStandardCategory(valueElement.text().trim());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("解析基础信息失败", e);
        }
    }

    /**
     * 解析备案信息
     */
    private void parseRecordInfo(Document doc, LocalStandardDetailInfo info) {
        try {
            // 查找备案信息段落
            Element recordSection = doc.select("div.para-title:contains(备案信息)").first();
            if (recordSection != null) {
                // 备案信息段落后面直接跟着p标签
                Element current = recordSection.nextElementSibling();
                while (current != null) {
                    if (current.tagName().equals("p")) {
                        String text = current.text().trim();
                        
                        if (text.startsWith("备案号：")) {
                            info.setRecordNumber(text.substring(4));
                        } else if (text.startsWith("备案日期：")) {
                            info.setRecordDate(text.substring(5));
                        } else if (text.startsWith("备案月报：")) {
                            // 提取链接文本
                            Element link = current.select("a").first();
                            if (link != null) {
                                info.setRecordBulletin(link.text().trim());
                            } else {
                                info.setRecordBulletin(text.substring(5));
                            }
                        }
                    } else if (current.hasClass("para-title")) {
                        // 遇到下一个段落，停止解析
                        break;
                    }
                    current = current.nextElementSibling();
                }
            }
        } catch (Exception e) {
            log.debug("解析备案信息失败", e);
        }
    }

    /**
     * 解析适用范围
     */
    private void parseScope(Document doc, LocalStandardDetailInfo info) {
        try {
            // 查找适用范围段落
            Element scopeSection = doc.select("div.para-title:contains(适用范围)").first();
            if (scopeSection != null) {
                Element current = scopeSection.nextElementSibling();
                while (current != null) {
                    if (current.tagName().equals("p")) {
                        String scopeText = current.text().trim();
                        info.setScope(scopeText);
                        break;
                    } else if (current.hasClass("para-title")) {
                        break;
                    }
                    current = current.nextElementSibling();
                }
            }
        } catch (Exception e) {
            log.debug("解析适用范围失败", e);
        }
    }

    /**
     * 解析起草信息
     */
    private void parseDraftingInfo(Document doc, LocalStandardDetailInfo info) {
        try {
            // 解析起草单位
            Element unitsSection = doc.select("div.para-title:contains(起草单位)").first();
            if (unitsSection != null) {
                Element current = unitsSection.nextElementSibling();
                while (current != null) {
                    if (current.tagName().equals("p")) {
                        String unitsText = current.text().trim();
                        if (!unitsText.isEmpty() && !unitsText.equals(" ")) {
                            info.setDraftingUnits(unitsText);
                        }
                        break;
                    } else if (current.hasClass("para-title")) {
                        break;
                    }
                    current = current.nextElementSibling();
                }
            }
            
            // 解析起草人
            Element personsSection = doc.select("div.para-title:contains(起草人)").first();
            if (personsSection != null) {
                Element current = personsSection.nextElementSibling();
                while (current != null) {
                    if (current.tagName().equals("p")) {
                        String personsText = current.text().trim();
                        if (!personsText.isEmpty() && !personsText.equals(" ")) {
                            info.setDraftingPersons(personsText);
                        }
                        break;
                    } else if (current.hasClass("para-title")) {
                        break;
                    }
                    current = current.nextElementSibling();
                }
            }
        } catch (Exception e) {
            log.debug("解析起草信息失败", e);
        }
    }
}
