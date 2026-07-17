package com.zhouchuanxiang.outputtoolkit.agentrag.session;

import com.zhouchuanxiang.outputtoolkit.agentrag.entity.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息数据访问层
 * <p>
 * 使用 JdbcTemplate 操作 t_message 表，记录每轮对话的完整交互。
 * </p>
 *
 * @author qifan
 * @since 2026-07-13
 */
@Slf4j
@Repository
public class MessageMapper {

    private final JdbcTemplate jdbcTemplate;

    public MessageMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 消息行映射器
     */
    private final RowMapper<Message> rowMapper = (rs, rowNum) -> Message.builder()
            .id(rs.getLong("id"))
            .conversationId(rs.getLong("conversation_id"))
            .role(rs.getString("role"))
            .content(rs.getString("content"))
            .toolName(rs.getString("tool_name"))
            .tokenCount(rs.getObject("token_count") != null ? rs.getInt("token_count") : null)
            .createdAt(rs.getObject("created_at", LocalDateTime.class))
            .build();

    /**
     * 插入一条消息
     *
     * @param message 消息对象
     * @return 受影响行数
     */
    public int insert(Message message) {
        String sql = """
                INSERT INTO t_message (conversation_id, role, content, tool_name, token_count, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        return jdbcTemplate.update(sql,
                message.getConversationId(),
                message.getRole(),
                message.getContent(),
                message.getToolName(),
                message.getTokenCount(),
                Timestamp.valueOf(LocalDateTime.now()));
    }

    /**
     * 查询会话的最近 N 条消息（用于构建上下文窗口）
     * <p>
     * 按创建时间倒序取前 N 条，再反转得到时间正序列表。
     * 这样 LLM 看到的对话历史就是按时间先后排列的。
     * </p>
     *
     * @param conversationId 会话ID
     * @param limit          最大消息数
     * @return 时间正序的消息列表
     */
    //tips: 上下文窗口管理是 RAG Agent 的关键性能优化点。
    //     LLM 是按 token 收费的，每次请求都会把历史消息带上。
    //     如果不加限制，对话 100 轮后，每次请求可能要花几十万 token！
    //     这里的做法是只保留最近 N 条消息（滑动窗口），
    //     就像你看聊天记录时只翻最近几页，太老的就不翻了。
    public List<Message> selectRecentByConversationId(Long conversationId, int limit) {
        String sql = """
                SELECT id, conversation_id, role, content, tool_name, token_count, created_at
                FROM (
                    SELECT * FROM t_message
                    WHERE conversation_id = ?
                    ORDER BY created_at DESC
                    LIMIT ?
                ) sub
                ORDER BY created_at ASC
                """;
        return jdbcTemplate.query(sql, rowMapper, conversationId, limit);
    }

    /**
     * 查询会话的所有消息
     *
     * @param conversationId 会话ID
     * @return 时间正序的消息列表
     */
    public List<Message> selectByConversationId(Long conversationId) {
        String sql = """
                SELECT id, conversation_id, role, content, tool_name, token_count, created_at
                FROM t_message
                WHERE conversation_id = ?
                ORDER BY created_at ASC
                """;
        return jdbcTemplate.query(sql, rowMapper, conversationId);
    }

    /**
     * 删除会话下的所有消息
     *
     * @param conversationId 会话ID
     * @return 删除条数
     */
    public int deleteByConversationId(Long conversationId) {
        String sql = "DELETE FROM t_message WHERE conversation_id = ?";
        return jdbcTemplate.update(sql, conversationId);
    }
}
