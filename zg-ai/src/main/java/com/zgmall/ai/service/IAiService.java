package com.zgmall.ai.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface IAiService {

    /**
     * AI 导购流式问答（SSE）
     * 链路：embedding -> Milvus 检索 -> 组装 prompt（rag-prompt-template.st）-> GLM 生成 -> 流式输出
     * 事件协议：data 文本片段 / event: items + 商品JSON / data: [DONE]
     */
    SseEmitter chatStream(String message, String sessionId);
}
