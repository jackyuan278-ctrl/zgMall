package com.zgmall.ai.controller;

import com.zgmall.ai.service.IKnowledgeIngestService;
import com.zgmall.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 知识入库手动触发（内部运维口子，走网关需登录；商品数据变更后重跑一次）
 */
@RestController
@RequestMapping("/ai/admin")
@RequiredArgsConstructor
public class AiAdminController {

    private final IKnowledgeIngestService knowledgeIngestService;

    @PostMapping("/ingest")
    public Result<Void> ingest() {
        knowledgeIngestService.ingestItems();
        return Result.success();
    }
}
