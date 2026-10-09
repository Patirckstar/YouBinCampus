package com.youbin.agent;

import cn.hutool.json.JSONObject;
import com.youbin.dto.AgentGoodsResult;
import com.youbin.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 智能发布助手：文字描述 → 结构化商品字段
 */
@Service
@RequiredArgsConstructor
public class GoodsAgentService {

    private final LlmClient llmClient;
    private final CategoryService categoryService;

    public AgentGoodsResult generate(String text) {
        String raw = llmClient.chat(PromptTemplate.GOODS_SYSTEM, text);
        JSONObject obj = LlmClient.extractJson(raw);

        AgentGoodsResult r = new AgentGoodsResult();
        r.setTitle(obj.getStr("title"));
        r.setDescription(obj.getStr("description"));
        r.setCategoryName(obj.getStr("category"));
        String price = obj.getStr("price");
        if (price != null && !price.isBlank()) {
            try {
                r.setPrice(new BigDecimal(price.trim()));
            } catch (Exception ignore) {
                // 价格解析失败时置空，由前端补充
            }
        }
        r.setCategoryId(categoryService.matchCategoryId(r.getCategoryName()));
        return r;
    }
}