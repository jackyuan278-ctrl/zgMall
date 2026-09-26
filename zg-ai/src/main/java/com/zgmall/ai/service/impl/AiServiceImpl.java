package com.zgmall.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zgmall.ai.service.IAiService;
import com.zgmall.api.client.ItemClient;
import com.zgmall.api.dto.ItemDTO;
import com.zgmall.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 导购流式问答。RAG 检索 + 会话记忆已由 AiConfig 的 advisor 链（QuestionAnswerAdvisor +
 * MessageChatMemoryAdvisor）自动完成，这里只负责：
 * 1) 单独 similaritySearch 拿候选商品 -> Feign 实时查 -> event: items（价格库存以真实数据为准，不让 LLM 编）
 * 2) ChatClient.stream() 流式吐文本片段 -> data
 * 3) 结尾 data: [DONE]
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements IAiService {

    private final ChatClient shopClient;
    private final VectorStore vectorStore;
    private final ItemClient itemClient;
    private final ObjectMapper objectMapper;

    private static final long SSE_TIMEOUT = 60_000L;
    private static final int ITEM_TOP_K = 4;
    private static final double ITEM_THRESHOLD = 0.4;

    @Override
    public SseEmitter chatStream(String message, String sessionId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        // 1. 先推「推荐商品卡片」：向量检索   候选 itemId -> Feign 实时查真实价格库存
        try {
            List<Map<String, Object>> items = searchRecommendItems(message);
            if (!items.isEmpty()) {
                emitter.send(SseEmitter.event().name("items").data(objectMapper.writeValueAsString(items)));
            }
        } catch (Exception e) {
            // 商品卡片推送失败不影响后续文字回答
            log.warn("推送 items 事件失败: {}", e.getMessage());
        }

        // 2. 流式生成回答。advisor 链自动做记忆 + RAG，必须传 CONVERSATION_ID 按会话隔离
        shopClient.prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
                .stream()
                .content()
                .subscribe(
                        chunk -> {
                            try {
                                emitter.send(SseEmitter.event().data(chunk));
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        error -> {
                            log.error("流式生成异常", error);
                            emitter.completeWithError(error);
                        },
                        () -> {
                            try {
                                emitter.send(SseEmitter.event().data("[DONE]"));
                            } catch (IOException ignored) {
                                // 客户端已断开，忽略
                            }
                            emitter.complete();
                        }
                );

        return emitter;
    }

    /**
     * 检索候选商品并实时补全真实字段。metadata 只用来拿 itemId，
     * 价格/库存/图片一律走 Feign 查最新值，避免向量库里的旧快照被当成事实。
     */
    private List<Map<String, Object>> searchRecommendItems(String message) {
        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(message)
                        .topK(ITEM_TOP_K)
                        .similarityThreshold(ITEM_THRESHOLD)
                        .build());
        List<Map<String, Object>> items = new ArrayList<>();
        if (docs == null || docs.isEmpty()) {
            return items;
        }
        for (Document doc : docs) {
            Object itemIdObj = doc.getMetadata().get("itemId");
            if (itemIdObj == null) {
                continue;
            }
            Long itemId = Long.valueOf(String.valueOf(itemIdObj));
            Map<String, Object> card = new LinkedHashMap<>();
            Result<ItemDTO> result = itemClient.queryItemById(itemId);
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                ItemDTO item = result.getData();
                card.put("id", item.getId());
                card.put("name", item.getName());
                card.put("price", item.getPrice());
                card.put("image", item.getImage());
                card.put("stock", item.getStock());
            } else {
                // Feign 查不到时退回向量库快照，保证卡片不空
                card.put("id", itemId);
                card.put("name", doc.getMetadata().get("name"));
                card.put("price", doc.getMetadata().get("price"));
                card.put("image", doc.getMetadata().get("image"));
                card.put("stock", doc.getMetadata().get("stock"));
            }
            items.add(card);
        }
        return items;
    }
}
