package com.zhouchuanxiang.outputtoolkit.agentrag.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 聊天请求 DTO
 *
 * @author qifan
 * @since 2026-07-13
 */
@Data
public class ChatRequest {

    /** 用户问题（必填） */
    @NotBlank(message = "问题不能为空")
    private String question;

    /** 会话ID（可选，不传则创建新会话） */
    private Long conversationId;

    /** 用户标识（默认 "default"） */
    private String userId = "default";
}
