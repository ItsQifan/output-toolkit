package com.zhouchuanxiang.outputtoolkit.agentrag.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会话实体
 * <p>
 * 每个用户可创建多个独立会话，每个会话有独立的消息历史和上下文。
 * 对应数据库表 t_conversation。
 * </p>
 *
 * @author qifan
 * @since 2026-07-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {

    /** 会话ID（数据库自增） */
    private Long id;

    /** 用户标识（默认 "default"，后续可扩展多用户） */
    private String userId;

    /** 会话标题（取首条用户问题的前30个字符） */
    private String title;

    /** 会话状态：1=进行中, 0=已归档 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 最后更新时间 */
    private LocalDateTime updatedAt;
}
