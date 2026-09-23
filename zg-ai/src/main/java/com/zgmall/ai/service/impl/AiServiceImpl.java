package com.zgmall.ai.service.impl;

import com.zgmall.ai.service.IAiService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class AiServiceImpl implements IAiService {

    @Override
    public SseEmitter chatStream(String message, String sessionId) {
        // TODO 核心业务待用户实现（项目压轴面试点）：
        //      1. 商品知识向量化入库任务（读 tb_item 拼知识文档 -> GLM embedding-3（2048 维）->
        //         Milvus vector_store，metadata 带 itemId/price）——可先做一次性脚本/CommandLineRunner
        //      2. 用户问题向量化 -> Milvus 相似检索 top-k
        //      3. 用 prompts/rag-prompt-template.st 组装 {context}/{question}，ChatClient 加载
        //         prompts/system.st 人设 -> stream() 流式生成
        //      4. SseEmitter 输出：data 文本片段；event: items 推推荐商品（商品数据来自检索
        //         metadata + Feign 实时查询，不得让 LLM 编造价格库存）；结尾 data: [DONE]
        //      5. 会话记忆：Redis db2 按 sessionId 存最近 N 轮，超长截断拼进 prompt
        throw new UnsupportedOperationException("TODO: AI 导购流式问答待实现");
    }
}
