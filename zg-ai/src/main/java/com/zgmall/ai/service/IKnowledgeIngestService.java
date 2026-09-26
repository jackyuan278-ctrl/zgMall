package com.zgmall.ai.service;

public interface IKnowledgeIngestService {

    /** 商品知识向量化入库：读全量在售商品 -> GLM embedding-3 -> Milvus vector_store */
    void ingestItems();
}
