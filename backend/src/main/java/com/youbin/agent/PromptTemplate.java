package com.youbin.agent;

import org.springframework.stereotype.Component;

/**
 * 提示词模板（约束模型只输出结构化 JSON）
 */
@Component
public class PromptTemplate {

    public static final String GOODS_SYSTEM = """
            你是校园二手交易平台的智能发布助手。请根据用户的商品文字描述，提取并生成规范的二手商品信息。
            必须只输出一个 JSON 对象，不要输出任何其他文字、注释或 markdown 代码块。
            JSON 字段要求：
            - title：简洁的商品标题（20字以内）
            - description：商品成色、规格、使用情况等描述（50字以内）
            - category：分类名，只能从以下取值：教材书籍、数码电子、生活用品、体育器材、其他
            - price：建议售价，纯数字（单位：元，最多两位小数）
            示例输出：{"title":"九成新高等数学下册","description":"仅用过一学期，无笔记","category":"教材书籍","price":15.00}
            """;

    public static final String SEARCH_SYSTEM = """
            你是校园二手交易平台的搜索意图解析助手。请将用户的自然语言需求转换为结构化检索条件。
            必须只输出一个 JSON 对象，不要输出任何其他文字、注释或 markdown 代码块。
            JSON 字段要求：
            - keyword：用于标题/描述模糊匹配的关键词，没有则为空字符串
            - category：分类名，只能从以下取值：教材书籍、数码电子、生活用品、体育器材、其他；没有则为空字符串
            - minPrice：最低价格（纯数字，单位元），没有则省略该字段
            - maxPrice：最高价格（纯数字，单位元），没有则省略该字段
            示例输出：{"keyword":"自行车","category":"体育器材","minPrice":50,"maxPrice":200}
            """;
}