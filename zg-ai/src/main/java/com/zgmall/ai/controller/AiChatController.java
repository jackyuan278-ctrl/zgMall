package com.zgmall.ai.controller;

import com.zgmall.ai.service.IAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final IAiService aiService;

    /**
     * SSE 流式对话。
     * 注意：浏览器 EventSource 无法自定义请求头，token 由前端放在 query 参数 authorization 里，
     * 网关需支持从 query 兜底取 token（见 docs/api.md 3.6.1）。
     */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestParam("message") String message,
                                 @RequestParam("sessionId") String sessionId) {
        return aiService.chatStream(message, sessionId);
    }
}
