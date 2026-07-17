package com.zhouchuanxiang.outputtoolkit.agentrag.session;

import com.zhouchuanxiang.outputtoolkit.agentrag.entity.Conversation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 会话数据访问层
 * <p>
 * 使用 JdbcTemplate 操作 t_conversation 表，与 mcp-nl2sql 模块保持一致的 DAO 风格。
 * </p>
 *
 * @author qifan
 * @since 2026-07-13
 */
@Slf4j
@Repository
public class ConversationMapper {

    private final JdbcTemplate jdbcTemplate;

    public ConversationMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 会话行映射器 —— 将数据库行转换为 Conversation 对象
     */
    private final RowMapper<Conversation> rowMapper = (rs, rowNum) -> Conversation.builder()
            .id(rs.getLong("id"))
            .userId(rs.getString("user_id"))
            .title(rs.getString("title"))
            .status(rs.getInt("status"))
            .createdAt(rs.getObject("created_at", LocalDateTime.class))
            .updatedAt(rs.getObject("updated_at", LocalDateTime.class))
            .build();

    /**
     * 创建新会话
     *
     * @param conversation 会话对象（id 会被自动填充）
     * @return 受影响行数
     */
    public int insert(Conversation conversation) {
        String sql = """
                INSERT INTO t_conversation (user_id, title, status, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int rows = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, conversation.getUserId());
            ps.setString(2, conversation.getTitle());
            ps.setInt(3, conversation.getStatus() != null ? conversation.getStatus() : 1);
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            return ps;
        }, keyHolder);

        // 回填自增ID
        if (keyHolder.getKey() != null) {
            conversation.setId(keyHolder.getKey().longValue());
        }
        log.info("会话管理_创建会话成功, conversationId={}, title={}", conversation.getId(), conversation.getTitle());
        return rows;
    }

    /**
     * 根据会话ID查询
     *
     * @param id 会话ID
     * @return 会话对象，不存在返回 null
     */
    public Conversation selectById(Long id) {
        String sql = "SELECT id, user_id, title, status, created_at, updated_at FROM t_conversation WHERE id = ?";
        List<Conversation> list = jdbcTemplate.query(sql, rowMapper, id);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 查询用户的所有会话（按更新时间倒序）
     *
     * @param userId 用户标识
     * @return 会话列表
     */
    public List<Conversation> selectByUserId(String userId) {
        String sql = """
                SELECT id, user_id, title, status, created_at, updated_at
                FROM t_conversation
                WHERE user_id = ? AND status = 1
                ORDER BY updated_at DESC
                """;
        return jdbcTemplate.query(sql, rowMapper, userId);
    }

    /**
     * 更新会话标题
     *
     * @param id    会话ID
     * @param title 新标题
     * @return 受影响行数
     */
    public int updateTitle(Long id, String title) {
        String sql = "UPDATE t_conversation SET title = ?, updated_at = ? WHERE id = ?";
        return jdbcTemplate.update(sql, title, Timestamp.valueOf(LocalDateTime.now()), id);
    }

    /**
     * 更新会话的最后更新时间
     *
     * @param id 会话ID
     */
    public void touch(Long id) {
        String sql = "UPDATE t_conversation SET updated_at = ? WHERE id = ?";
        jdbcTemplate.update(sql, Timestamp.valueOf(LocalDateTime.now()), id);
    }

    /**
     * 删除会话（软删除，设置 status=0）
     *
     * @param id 会话ID
     * @return 受影响行数
     */
    public int deleteById(Long id) {
        String sql = "UPDATE t_conversation SET status = 0, updated_at = ? WHERE id = ?";
        log.info("会话管理_删除会话, conversationId={}", id);
        return jdbcTemplate.update(sql, Timestamp.valueOf(LocalDateTime.now()), id);
    }
}
