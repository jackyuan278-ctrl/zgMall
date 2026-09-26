package com.zgmall.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor; // M8重点！vectorstore子包
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Spring AI Bean 装配。
 * VectorStore / EmbeddingModel 由 zhipuai + milvus starter 自动装配（勿手建，会和自动配置冲突），
 * 业务里直接注入 VectorStore 接口即可。
 * - chatMemory：窗口记忆 10 条；M4 时把 InMemory repo 换成 Redis 实现（契约 db2 按 sessionId）
 * - shopClient：人设 = prompts/system.st；advisor 链 = 日志 + 记忆 + RAG 检索
 * ⚠ 调用方每次请求必须传 .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))，
 *   不传的话所有用户共用一个默认会话，记忆全串。
 */
@Configuration
public class AiConfig {

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(10)
                .build();
    }

    @Bean
    public ChatClient shopClient(ChatModel model,
                                 VectorStore vectorStore,
                                 @Value("classpath:prompts/system.st") Resource systemPrompt) {
        String systemText;
        try (InputStream is = systemPrompt.getInputStream()) {
            systemText = StreamUtils.copyToString(is, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("读取system.st系统提示词文件失败", e);
        }
        return ChatClient.builder(model)
                .defaultSystem(systemText)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        // 记忆不走 advisor：1.0.0 GA 的 MessageChatMemoryAdvisor 在 stream 路径不保存对话，
                        // 改由 AiServiceImpl 手动 get/add ChatMemory（窗口截断仍由 MessageWindowChatMemory 负责）
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder()
                                        .topK(4)
                                        .similarityThreshold(0.6)
                                        .build())
                                .build()
                )
                .build();
    }
}
