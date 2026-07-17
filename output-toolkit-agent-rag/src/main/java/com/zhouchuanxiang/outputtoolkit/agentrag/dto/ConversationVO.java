package com.zhouchuanxiang.outputtoolkit.agentrag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会话视图对象
 *
 * @author qifan
 * @since 2026-07-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationVO {

    private Long id;
    private String userId;
    private String title;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
