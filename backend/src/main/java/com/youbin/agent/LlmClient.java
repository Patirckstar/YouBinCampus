package com.youbin.agent;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 大模型客户端（OpenAI 兼容 /chat/completions 接口）
 * 支持 DeepSeek、通义千问（兼容模式）、GLM、SiliconFlow 等
 */
@Slf4j
@Component
public class LlmClient {

    @Value("${llm.base-url:}")
    private String baseUrl;

    @Value("${llm.api-key:}")
    private String apiKey;

    @Value("${llm.model:deepseek-chat}")
    private String model;

    public String chat(String systemPrompt, String userPrompt) {
        if (baseUrl == null || baseUrl.isBlank() || apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("未配置大模型，请先在 application-secret.yml 填写 llm.base-url / llm.api-key");
        }

        JSONObject body = JSONUtil.createObj()
                .set("model", model)
                .set("messages", List.of(
                        JSONUtil.createObj().set("role", "system").set("content", systemPrompt),
                        JSONUtil.createObj().set("role", "user").set("content", userPrompt)))
                .set("temperature", 0.3);

        String url = baseUrl.endsWith("/") ? baseUrl + "chat/completions" : baseUrl + "/chat/completions";
        try (HttpResponse resp = HttpRequest.post(url)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(body.toString())
                .timeout(30000)
                .execute()) {
            String respBody = resp.body();
            JSONObject json = JSONUtil.parseObj(respBody);
            if (json.get("choices") != null) {
                return json.getJSONArray("choices").getJSONObject(0)
                        .getJSONObject("message").getStr("content");
            }
            log.error("LLM 调用异常：{}", respBody);
            throw new RuntimeException("大模型调用失败：" + respBody);
        }
    }

    /**
     * 从模型原始输出中提取 JSON 对象（容忍 markdown 代码块等噪声）
     */
    public static JSONObject extractJson(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new RuntimeException("大模型未返回内容");
        }
        String s = raw.trim();
        int start = s.indexOf('{');
        int end = s.lastIndexOf('}');
        if (start >= 0 && end > start) {
            s = s.substring(start, end + 1);
        }
        return JSONUtil.parseObj(s);
    }
}