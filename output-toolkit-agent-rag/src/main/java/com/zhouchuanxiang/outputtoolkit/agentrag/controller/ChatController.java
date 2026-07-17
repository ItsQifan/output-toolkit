package com.zhouchuanxiang.outputtoolkit.agentrag.controller;

import com.zhouchuanxiang.outputtoolkit.agentrag.agent.AgentService;
import com.zhouchuanxiang.outputtoolkit.agentrag.dto.ChatEvent;
import com.zhouchuanxiang.outputtoolkit.agentrag.dto.ChatRequest;
import com.zhouchuanxiang.outputtoolkit.agentrag.entity.Conversation;
import com.zhouchuanxiang.outputtoolkit.agentrag.entity.Message;
import com.zhouchuanxiang.outputtoolkit.agentrag.session.SessionService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 聊天控制器
 * <p>
 * 提供 Agent 对话的核心 API：SSE 流式聊天，支持推理链可视化。
 * 日志前缀：Agent对话_
 * </p>
 * <p>
 * SSE（Server-Sent Events）是一种单向流式传输协议，
 * 服务器可以持续向客户端推送事件，客户端通过 EventSource API 接收。
 * 相比 WebSocket 更轻量，适合"服务器→客户端"的单向推送场景。
 * </p>
 *
 * @author qifan
 * @since 2026-07-13
 */
//tips: SSE（Server-Sent Events）是一种让服务器"主动推消息给浏览器"的技术。
//     普通 HTTP 请求是问一句答一句，SSE 是问一句后服务器可以连续回答好多条。
//     在这个 Agent 项目中，SSE 用来实现两个效果：
//     1. 实时展示 Agent 的"思考过程"（检索→调工具→分析→回答），
//        而不是等所有步骤完成才一次性显示。
//     2. 实现打字机效果——LLM 的回复一个字一个字地显示。
//     前端用 EventSource API 接收，非常简单：
//     const es = new EventSource('/api/chat/stream?question=xxx');
//     es.addEventListener('thinking', e => showThinking(e.data));
//     es.addEventListener('answer', e => appendAnswer(e.data));
@Slf4j
@RestController
@RequestMapping("/api")
public class ChatController {

    private final AgentService agentService;
    private final SessionService sessionService;

    public ChatController(AgentService agentService, SessionService sessionService) {
        this.agentService = agentService;
        this.sessionService = sessionService;
    }

    /**
     * SSE 流式聊天
     * <p>
     * 前端通过 EventSource 连接到这个端点，
     * 服务端通过 SseEmitter 逐条推送 thinking / tool_call / answer / done 事件。
     * </p>
     * <p>
     * 超时设置：60秒（60000ms），防止连接无限制占用资源。
     * 如果 LLM 处理超过60秒，前端应支持重连并继续展示。
     * </p>
     *
     * @param question       用户问题
     * @param conversationId 会话ID（可选）
     * @param userId         用户标识（默认 "default"）
     * @return SseEmitter 实例
     */
    @GetMapping("/chat/stream")
    public SseEmitter chatStream(
            @RequestParam String question,
            @RequestParam(required = false) Long conversationId,
            @RequestParam(defaultValue = "default") String userId) {

        SseEmitter emitter = new SseEmitter(60_000L);
        log.info("Agent对话_SSE连接建立, question={}, conversationId={}",
                question.length() > 50 ? question.substring(0, 50) + "..." : question, conversationId);

        // 异步执行 Agent（避免阻塞主线程）
        //tips: 为什么用异步？因为 Agent 的完整执行流程可能需要 5~30 秒
        //     （RAG检索 + LLM调用 + 可能的工具调用），如果同步执行，
        //     会一直占用 Tomcat 的工作线程。异步执行让请求线程立即返回，
        //     Agent 在后台线程中执行，结果通过 SseEmitter 推送。
        new Thread(() -> {
            try {
                agentService.executeStream(conversationId, userId, question, event -> {
                    try {
                        emitter.send(SseEmitter.event()
                                .name(event.getType())
                                .data(event.getData()));
                    } catch (IOException e) {
                        log.error("Agent对话_SSE推送失败", e);
                    }
                });
                emitter.complete();
                log.info("Agent对话_SSE连接完成, conversationId={}", conversationId);
            } catch (Exception e) {
                log.error("Agent对话_执行失败", e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("{\"message\":\"" + e.getMessage() + "\"}"));
                    emitter.complete();
                } catch (IOException ex) {
                    emitter.completeWithError(ex);
                }
            }
        }, "agent-chat-thread").start();

        return emitter;
    }

    /**
     * 普通聊天接口（非流式，返回完整 JSON）
     *
     * @param request 聊天请求
     * @return 包含回答的 JSON
     */
    @PostMapping("/chat")
    public Map<String, Object> chat(@Valid @RequestBody ChatRequest request) {
        log.info("Agent对话_普通聊天请求, question={}", request.getQuestion());

        var result = agentService.executeStream(
                request.getConversationId(),
                request.getUserId(),
                request.getQuestion(),
                event -> {
                } // 非流式模式不推送事件
        );

        return Map.of(
                "code", 200,
                "data", Map.of(
                        "conversationId", result.conversationId(),
                        "answer", result.answer(),
                        "tokenUsed", result.tokenUsed()
                )
        );
    }

    /**
     * 获取会话列表
     *
     * @param userId 用户标识
     * @return 会话列表
     */
    @GetMapping("/conversations")
    public Map<String, Object> listConversations(@RequestParam(defaultValue = "default") String userId) {
        List<Conversation> conversations = sessionService.listConversations(userId);
        return Map.of("code", 200, "data", conversations);
    }

    /**
     * 获取会话的历史消息
     *
     * @param id 会话ID
     * @return 消息列表
     */
    @GetMapping("/conversations/{id}/messages")
    public Map<String, Object> getMessages(@PathVariable Long id) {
        List<Message> messages = sessionService.getAllMessages(id);
        return Map.of("code", 200, "data", messages);
    }

    /**
     * 删除会话
     *
     * @param id 会话ID
     * @return 操作结果
     */
    @DeleteMapping("/conversations/{id}")
    public Map<String, Object> deleteConversation(@PathVariable Long id) {
        sessionService.deleteConversation(id);
        return Map.of("code", 200, "msg", "删除成功");
    }
}
