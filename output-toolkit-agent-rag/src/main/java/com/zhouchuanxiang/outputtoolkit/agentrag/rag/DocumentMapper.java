package com.zhouchuanxiang.outputtoolkit.agentrag.rag;

import com.zhouchuanxiang.outputtoolkit.agentrag.entity.KnowledgeDocument;
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
 * 文档状态数据访问层
 * <p>
 * 使用 JdbcTemplate 操作 t_document 表，与 ConversationMapper 保持一致的 DAO 风格。
 * 支撑 Kafka 异步文档处理流水线的状态流转：PENDING → PROCESSING → COMPLETED/FAILED。
 * </p>
 *
 * @author qifan
 * @since 2026-07-16
 */
@Slf4j
@Repository
public class DocumentMapper {

    private final JdbcTemplate jdbcTemplate;

    public DocumentMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 文档行映射器 —— 将数据库行转换为 KnowledgeDocument 对象
     */
    private final RowMapper<KnowledgeDocument> rowMapper = (rs, rowNum) -> KnowledgeDocument.builder()
            .id(rs.getLong("id"))
            .filename(rs.getString("filename"))
            .filePath(rs.getString("file_path"))
            .status(rs.getString("status"))
            .chunkCount(rs.getInt("chunk_count"))
            .errorMsg(rs.getString("error_msg"))
            .createdAt(rs.getObject("created_at", LocalDateTime.class))
            .updatedAt(rs.getObject("updated_at", LocalDateTime.class))
            .build();

    /**
     * 新增文档记录（初始状态 PENDING）
     *
     * @param document 文档对象（id 会被自动回填）
     * @return 受影响行数
     */
    public int insert(KnowledgeDocument document) {
        String sql = """
                INSERT INTO t_document (filename, file_path, status, chunk_count, created_at, updated_at)
                VALUES (?, ?, ?, 0, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int rows = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, document.getFilename());
            ps.setString(2, document.getFilePath());
            ps.setString(3, document.getStatus() != null ? document.getStatus() : KnowledgeDocument.STATUS_PENDING);
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            return ps;
        }, keyHolder);

        // 回填自增ID，供上传接口返回给前端做状态轮询
        if (keyHolder.getKey() != null) {
            document.setId(keyHolder.getKey().longValue());
        }
        log.info("文档管理_创建文档记录, documentId={}, filename={}", document.getId(), document.getFilename());
        return rows;
    }

    /**
     * 根据文档ID查询
     *
     * @param id 文档ID
     * @return 文档对象，不存在返回 null
     */
    public KnowledgeDocument selectById(Long id) {
        String sql = """
                SELECT id, filename, file_path, status, chunk_count, error_msg, created_at, updated_at
                FROM t_document WHERE id = ?
                """;
        List<KnowledgeDocument> list = jdbcTemplate.query(sql, rowMapper, id);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 根据文件名查询（同名文档取最新一条）
     *
     * @param filename 文档文件名
     * @return 文档对象，不存在返回 null
     */
    public KnowledgeDocument selectByFilename(String filename) {
        String sql = """
                SELECT id, filename, file_path, status, chunk_count, error_msg, created_at, updated_at
                FROM t_document WHERE filename = ? ORDER BY id DESC LIMIT 1
                """;
        List<KnowledgeDocument> list = jdbcTemplate.query(sql, rowMapper, filename);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 查询所有文档（按上传时间倒序）
     *
     * @return 文档列表
     */
    public List<KnowledgeDocument> selectAll() {
        String sql = """
                SELECT id, filename, file_path, status, chunk_count, error_msg, created_at, updated_at
                FROM t_document ORDER BY created_at DESC
                """;
        return jdbcTemplate.query(sql, rowMapper);
    }

    /**
     * 更新文档处理状态
     *
     * @param id     文档ID
     * @param status 新状态
     * @return 受影响行数
     */
    public int updateStatus(Long id, String status) {
        String sql = "UPDATE t_document SET status = ?, updated_at = ? WHERE id = ?";
        return jdbcTemplate.update(sql, status, Timestamp.valueOf(LocalDateTime.now()), id);
    }

    /**
     * 更新文档的存储路径（上传接口文件落地后回填）
     *
     * @param id       文档ID
     * @param filePath 文件存储路径
     * @return 受影响行数
     */
    public int updateFilePath(Long id, String filePath) {
        String sql = "UPDATE t_document SET file_path = ?, updated_at = ? WHERE id = ?";
        return jdbcTemplate.update(sql, filePath, Timestamp.valueOf(LocalDateTime.now()), id);
    }

    /**
     * 标记文档处理完成
     *
     * @param id         文档ID
     * @param chunkCount 分块数量
     * @return 受影响行数
     */
    public int markCompleted(Long id, int chunkCount) {
        String sql = "UPDATE t_document SET status = ?, chunk_count = ?, error_msg = NULL, updated_at = ? WHERE id = ?";
        return jdbcTemplate.update(sql, KnowledgeDocument.STATUS_COMPLETED, chunkCount,
                Timestamp.valueOf(LocalDateTime.now()), id);
    }

    /**
     * 标记文档处理失败
     *
     * @param id       文档ID
     * @param errorMsg 失败原因（超长时截断，避免超出字段长度）
     * @return 受影响行数
     */
    public int markFailed(Long id, String errorMsg) {
        // 截断超长异常信息，避免超出 error_msg 字段长度（1024）导致二次失败
        String truncated = errorMsg != null && errorMsg.length() > 1000 ? errorMsg.substring(0, 1000) : errorMsg;
        String sql = "UPDATE t_document SET status = ?, error_msg = ?, updated_at = ? WHERE id = ?";
        return jdbcTemplate.update(sql, KnowledgeDocument.STATUS_FAILED, truncated,
                Timestamp.valueOf(LocalDateTime.now()), id);
    }

    /**
     * 删除文档记录
     *
     * @param id 文档ID
     * @return 受影响行数
     */
    public int deleteById(Long id) {
        String sql = "DELETE FROM t_document WHERE id = ?";
        log.info("文档管理_删除文档记录, documentId={}", id);
        return jdbcTemplate.update(sql, id);
    }
}
