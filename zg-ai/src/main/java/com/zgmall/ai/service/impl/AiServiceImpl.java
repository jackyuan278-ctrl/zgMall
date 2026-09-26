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
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 导购流式问答。RAG 检索由 AiConfig 的 QuestionAnswerAdvisor 自动完成；会话记忆由本类手动
 * 读写 ChatMemory（1.0.0 GA 的 MessageChatMemoryAdvisor 在 stream 路径不保存对话）。职责：
 * 1) 单独 similaritySearch 拿候选商品 -> Feign 实时查 -> event: items（价格库存以真实数据为准，不让 LLM 编）
 * 2) 历史窗口 + 本轮问题 -> ChatClient.stream() 流式吐文本片段 -> data
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
    private final ChatMemory chatMemory;

    private static final long SSE_TIMEOUT = 60_000L;
    private static final int ITEM_TOP_K = 4;
    private static final double ITEM_THRESHOLD = 0.4;

    @Override
    public SseEmitter chatStream(String message, String sessionId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        // 1. 先推「推荐商品卡片」：向量检索   候选 itemId -> Feign 实时查真实价格库存
        List<Map<String, Object>> items;
        try {
            items = searchRecommendItems(message);
            if (!items.isEmpty()) {
                emitter.send(SseEmitter.event().name("items").data(objectMapper.writeValueAsString(items)));
            }
        } catch (Exception e) {
            items = List.of();
            // 商品卡片推送失败不影响后续文字回答
            log.warn("推送 items 事件失败: {}", e.getMessage());
        }

        // 2. 流式生成回答。记忆手动管理（1.0.0 GA 的 memory advisor 在 stream 路径不保存）：
        //    先取历史窗口塞进 prompt，流结束后把本轮问答写回
        String userPrompt = buildUserPrompt(message, items);
        List<Message> history = chatMemory.get(sessionId);
        StringBuilder fullReply = new StringBuilder();
        shopClient.prompt()
                .messages(history)
                .user(userPrompt)
                .stream()
                .content()
                .subscribe(
                        chunk -> {
                            fullReply.append(chunk);
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
                            chatMemory.add(sessionId, List.of(
                                    new UserMessage(userPrompt),
                                    new AssistantMessage(fullReply.toString())));
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
     * 把本次检索出的真实商品拼进用户消息，让文本推荐与前端商品卡片一一对应，
     * 避免 LLM 依据 RAG 上下文编出卡片里没有的商品。
     */
    private String buildUserPrompt(String message, List<Map<String, Object>> items) {
        if (items == null || items.isEmpty()) {
            return message;
        }
        StringBuilder sb = new StringBuilder(message);
        sb.append("\n\n【本回复可推荐的真实商品】\n");
        for (Map<String, Object> it : items) {
            Object price = it.get("price");
            double yuan = price == null ? 0 : Double.parseDouble(String.valueOf(price)) / 100.0;
            sb.append("- ").append(it.get("name"))
              .append("（价格 ").append(yuan).append(" 元，库存 ").append(it.get("stock")).append("）\n");
        }
        sb.append("\n要求：优先推荐上述商品，提到时用完整名称；商品卡片已展示给用户，回答里不必重复罗列参数。");
        return sb.toString();
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
            // Milvus 回读 metadata 数值为浮点（1002 -> "1002.0"），Long.valueOf 会炸，用 BigDecimal 兜住
            Long itemId = new BigDecimal(String.valueOf(itemIdObj)).longValueExact();
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
