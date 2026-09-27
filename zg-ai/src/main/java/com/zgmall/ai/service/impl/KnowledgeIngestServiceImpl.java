package com.zgmall.ai.service.impl;

import com.zgmall.ai.service.IKnowledgeIngestService;
import com.zgmall.api.client.ItemClient;
import com.zgmall.api.dto.ItemDTO;
import com.zgmall.common.Result;
import com.zgmall.common.domain.PageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeIngestServiceImpl implements IKnowledgeIngestService {

    private final ItemClient itemClient;
    private final VectorStore vectorStore;

    // 每批批量写入Milvus的文档数量，建议50~100
    private static final int BATCH_SIZE = 50;

    @Override
    public void ingestItems() {
        // ========== 幂等：先清空商品旧向量，否则已下架/已删除商品的向量会残留并被检索到
        // 不能写 "itemId IS NOT NULL"：Spring AI 的 FilterExpressionTextParser 没有 IS NULL 语法，
        // 会抛 FilterExpressionParseException。itemId != 0 会被转成 Milvus 的 metadata["itemId"] != 0
        vectorStore.delete("itemId != 0");

        int page = 1;
        int total = 0;
        Result<PageDTO<ItemDTO>> pageDTOResult;
        List<Document> batchDocs = new ArrayList<>(BATCH_SIZE);

        while (true) {
            pageDTOResult = itemClient.queryItemPage(page, 50);
            // 接口异常，告警并终止拉取
            if (pageDTOResult == null || pageDTOResult.getCode() != 200) {
                log.warn("拉取商品第{}页失败: {}", page, pageDTOResult == null ? "无响应" : pageDTOResult.getMsg());
                break;
            }
            PageDTO<ItemDTO> data = pageDTOResult.getData();
            List<ItemDTO> list = data == null ? null : data.getList();
            // 没有更多商品，退出循环
            if (list == null || list.isEmpty()) {
                break;
            }

            for (ItemDTO itemDTO : list) {
                // 1. 构建知识库文本：图片/ID 走 metadata，正文只放有语义的信息（规格/描述能提升口语查询命中）
                String content = String.format(
                        "商品名称：%s，价格：%.2f元，库存：%d，规格：%s，描述：%s",
                        itemDTO.getName(),
                        itemDTO.getPrice() / 100.0, // 分转元
                        itemDTO.getStock(),
                        itemDTO.getSpec(),
                        itemDTO.getDescription() == null ? "" : itemDTO.getDescription()
                );

                // 2. 构建元数据，检索结果可以带回这些字段
                Map<String, Object> meta = new HashMap<>();
                meta.put("itemId", itemDTO.getId());
                meta.put("name", itemDTO.getName());
                meta.put("price", itemDTO.getPrice());
                meta.put("image", itemDTO.getImage());
                meta.put("stock", itemDTO.getStock());

                // 3. 构造Document，文档ID用itemId实现幂等，重复id会覆盖
                Document doc = new Document(itemDTO.getId().toString(), content, meta);
                batchDocs.add(doc);

                // 攒够一批，写入向量库，清空批次
                if (batchDocs.size() >= BATCH_SIZE) {
                    vectorStore.add(batchDocs);
                    batchDocs.clear();
                }
            }
            total += list.size();
            page++;
        }

        // 循环结束，把剩下不足一批的文档入库
        if (!batchDocs.isEmpty()) {
            vectorStore.add(batchDocs);
        }
        log.info("商品知识入库完成，共处理 {} 条", total);
    }
}
