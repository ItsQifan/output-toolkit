package com.zhouchuanxiang.outputtoolkit.agentrag.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 消息实体
 * <p>
 * 记录每轮对话的完整交互，包括用户问题、助手回答、工具调用详情。
 * 对应数据库表 t_message。
 * </p>
 * <p>
 * 消息角色（role）说明：
 * <ul>
 *   <li><b>user</b>：用户提问</li>
 *   <li><b>assistant</b>：LLM 最终回答</li>
 *   <li><b>tool</b>：工具调用记录（tool_name 字段标记是哪个工具）</li>
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
public class Message {

    /** 消息ID（数据库自增） */
    private Long id;

    /** 所属会话ID */
    private Long conversationId;

    /** 消息角色：user / assistant / tool */
    private String role;

    /** 消息内容（Markdown 格式） */
    private String content;

    /** 工具名称（仅 role=tool 时有值，如 "DateTimeTool"、"OrderQueryTool"） */
    private String toolName;

    /** 预估 token 消耗数 */
    private Integer tokenCount;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
