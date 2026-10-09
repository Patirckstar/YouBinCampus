package com.youbin.agent;

import cn.hutool.json.JSONObject;
import com.youbin.common.PageResult;
import com.youbin.service.CategoryService;
import com.youbin.service.GoodsService;
import com.youbin.vo.GoodsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 自然语言搜索：需求 → 结构化条件 → 商品列表
 */
@Service
@RequiredArgsConstructor
public class SearchAgentService {

    private final LlmClient llmClient;
    private final CategoryService categoryService;
    private final GoodsService goodsService;

    public PageResult<GoodsVO> search(String query, int pageNum, int pageSize) {
        String raw = llmClient.chat(PromptTemplate.SEARCH_SYSTEM, query);
        JSONObject obj = LlmClient.extractJson(raw);

        String keyword = obj.getStr("keyword");
        Long categoryId = categoryService.matchCategoryId(obj.getStr("category"));
        BigDecimal minPrice = toDecimal(obj.get("minPrice"));
        BigDecimal maxPrice = toDecimal(obj.get("maxPrice"));

        return goodsService.page(pageNum, pageSize, categoryId, keyword, minPrice, maxPrice, null);
    }

    private BigDecimal toDecimal(Object v) {
        if (v == null) {
            return null;
        }
        try {
            return new BigDecimal(v.toString());
        } catch (Exception e) {
            return null;
        }
    }
}