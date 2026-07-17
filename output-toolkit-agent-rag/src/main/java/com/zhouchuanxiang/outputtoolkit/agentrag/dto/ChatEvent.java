package com.zhouchuanxiang.outputtoolkit.agentrag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SSE 事件 DTO
 * <p>
 * 每个 SSE 事件都有一个 type 和 data，前端根据 type 渲染不同的 UI 组件。
 * 事件类型：
 * <ul>
 *   <li>thinking —— Agent 正在思考/检索/推理</li>
 *   <li>tool_call —— Agent 正在调用工具</li>
 *   <li>tool_result —— 工具执行结果</li>
 *   <li>answer —— LLM 最终回答（可能分多个 delta 推送）</li>
 *   <li>error —— 执行出错</li>
 *   <li>done —— 对话完成</li>
 * </ul>
 * </p>
 *
 * @author qifan
 * @since 2026-07-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatEvent {

    /** 事件类型 */
    private String type;

    /** 事件数据（JSON 字符串） */
    private String data;

    // ===== 工厂方法 =====

    public static ChatEvent thinking(String message) {
        return ChatEvent.builder()
                .type("thinking")
                .data("{\"message\":\"" + escapeJson(message) + "\"}")
                .build();
    }

    public static ChatEvent toolCall(String toolName, String status) {
        return ChatEvent.builder()
                .type("tool_call")
                .data("{\"toolName\":\"" + escapeJson(toolName) + "\",\"status\":\"" + status + "\"}")
                .build();
    }

    public static ChatEvent answer(String content) {
        return ChatEvent.builder()
                .type("answer")
                .data("{\"content\":\"" + escapeJson(content) + "\"}")
                .build();
    }

    public static ChatEvent error(String message) {
        return ChatEvent.builder()
                .type("error")
                .data("{\"message\":\"" + escapeJson(message) + "\"}")
                .build();
    }

    public static ChatEvent done(Long conversationId, int tokenUsed) {
        return ChatEvent.builder()
                .type("done")
                .data("{\"conversationId\":" + conversationId + ",\"tokenUsed\":" + tokenUsed + "}")
                .build();
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
