package com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.service;

import com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.config.DbConfig;
import com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.db.QueryResult;
import com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.db.QueryResult.QueryType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

/**
 * SQL 执行核心服务
 * <p>
 * 负责执行 SQL 语句并返回格式化结果，参照 Python 版 mysql_mcp_server 的 run_query 逻辑：
 * <ul>
 *   <li>SHOW TABLES → 带表头的列表</li>
 *   <li>DESCRIBE/DESC/SHOW COLUMNS → CSV 格式（NULL → "NULL"）</li>
 *   <li>SELECT（有 ResultSet） → CSV 格式（NULL → ""）</li>
 *   <li>DML（INSERT/UPDATE/DELETE/DDL） → "Rows affected: N"</li>
 * </ul>
 * </p>
 *
 * @author qifan
 * @since 2026-07-09
 */
@Slf4j
@Service
public class QueryExecutionService {

    private final JdbcTemplate jdbcTemplate;
    private final DbConfig dbConfig;

    public QueryExecutionService(JdbcTemplate jdbcTemplate,
                                  DbConfig dbConfig) {
        this.jdbcTemplate = jdbcTemplate;
        this.dbConfig = dbConfig;
    }

    /**
     * 执行 SQL 查询并返回格式化结果
     *
     * @param sql 待执行的 SQL 语句
     * @return 格式化的查询结果
     */
    public QueryResult execute(String sql) {
        // 多语句检测 —— 去除末尾分号后检查是否还包含分号
        String trimmedSql = sql.trim();
        // 去除末尾分号
        String cleanedSql = trimmedSql.endsWith(";") ? trimmedSql.substring(0, trimmedSql.length() - 1).trim() : trimmedSql;
        if (cleanedSql.contains(";")) {
            throw new IllegalArgumentException("仅支持执行单条SQL语句，不支持多语句执行。请移除分号分隔的多条语句。");
        }

        // 判断查询类型
        QueryType queryType = detectQueryType(cleanedSql);

        log.info("SQL执行_开始执行, type={}, sql={}", queryType, cleanedSql.substring(0, Math.min(200, cleanedSql.length())));

        return switch (queryType) {
            case SHOW -> executeShow(cleanedSql);
            case DESCRIBE -> executeDescribe(cleanedSql);
            case SELECT -> executeSelect(cleanedSql);
            case DML -> {
                // 增删改开关校验
                String error = checkCrudSwitch(cleanedSql);
                if (error != null) {
                    throw new UnsupportedOperationException(error);
                }
                yield executeDml(cleanedSql);
            }
        };
    }

    /**
     * 检测 SQL 语句类型
     *
     * @param sql 清理后的 SQL（已去末尾分号）
     * @return 查询类型
     */
    private QueryType detectQueryType(String sql) {
        String upper = sql.toUpperCase().trim();

        // SHOW TABLES / SHOW DATABASES 等元数据查询
        if (upper.startsWith("SHOW TABLES") || upper.startsWith("SHOW DATABASES")
                || upper.startsWith("SHOW FULL TABLES")) {
            return QueryType.SHOW;
        }

        // SHOW INDEX / SHOW CREATE / SHOW STATUS 等其他 SHOW 语句 → 按 SELECT 处理
        if (upper.startsWith("SHOW ")) {
            return QueryType.SELECT;
        }

        // DESCRIBE / DESC / SHOW COLUMNS / EXPLAIN → 表结构查询
        if (upper.startsWith("DESCRIBE ") || upper.startsWith("DESC ")
                || upper.startsWith("SHOW COLUMNS") || upper.startsWith("EXPLAIN ")) {
            return QueryType.DESCRIBE;
        }

        // SELECT / WITH (CTE) → 查询
        if (upper.startsWith("SELECT ") || upper.startsWith("WITH ")
                || upper.startsWith("SELECT\n") || upper.startsWith("WITH\n")) {
            return QueryType.SELECT;
        }

        // INSERT / UPDATE / DELETE / CREATE / ALTER / DROP / TRUNCATE / SET 等 → DML
        return QueryType.DML;
    }

    /**
     * 校验 INSERT/UPDATE/DELETE 操作开关
     * <p>
     * 根据配置项 {@code mysql.insert-enabled} / {@code mysql.update-enabled} /
     * {@code mysql.delete-enabled} 判断当前 SQL 是否允许执行。
     * 仅校验增删改三种写操作，DDL（CREATE/ALTER/DROP 等）不受此开关限制。
     * </p>
     *
     * @param sql 清理后的 SQL 语句
     * @return 校验失败时返回错误信息，通过时返回 null
     */
    private String checkCrudSwitch(String sql) {
        String upper = sql.toUpperCase().trim();

        // 检测 INSERT 操作
        if (upper.startsWith("INSERT ")) {
            if (!dbConfig.isInsertEnabled()) {
                log.warn("SQL执行_INSERT操作已禁用, sql={}", sql.substring(0, Math.min(100, sql.length())));
                return "INSERT 操作已被管理员禁用，如需使用请联系管理员开启 mysql.insert-enabled=true";
            }
            return null;
        }

        // 检测 UPDATE 操作
        if (upper.startsWith("UPDATE ")) {
            if (!dbConfig.isUpdateEnabled()) {
                log.warn("SQL执行_UPDATE操作已禁用, sql={}", sql.substring(0, Math.min(100, sql.length())));
                return "UPDATE 操作已被管理员禁用，如需使用请联系管理员开启 mysql.update-enabled=true";
            }
            return null;
        }

        // 检测 DELETE 操作（注意 FROM 可有可无，如 DELETE FROM t 或 DELETE t）
        if (upper.startsWith("DELETE ")) {
            if (!dbConfig.isDeleteEnabled()) {
                log.warn("SQL执行_DELETE操作已禁用, sql={}", sql.substring(0, Math.min(100, sql.length())));
                return "DELETE 操作已被管理员禁用，如需使用请联系管理员开启 mysql.delete-enabled=true";
            }
            return null;
        }

        // DDL（CREATE/ALTER/DROP/TRUNCATE）不受开关限制，直接放行
        return null;
    }

    /**
     * 执行 SHOW TABLES 类查询 —— 返回带表头的列表
     * <p>
     * 参照 Python 版：SHOW 类查询直接返回列名 + 行数据的列表格式。
     * </p>
     */
    private QueryResult executeShow(String sql) {
        return jdbcTemplate.query(sql, (ResultSet rs) -> {
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            List<String> columns = new ArrayList<>();
            for (int i = 1; i <= colCount; i++) {
                columns.add(meta.getColumnName(i));
            }

            List<String[]> rows = new ArrayList<>();
            while (rs.next()) {
                String[] row = new String[colCount];
                for (int i = 0; i < colCount; i++) {
                    row[i] = rs.getString(i + 1);
                }
                rows.add(row);
            }

            log.info("SQL执行_SHOW结果, rows={}", rows.size());
            return QueryResult.builder()
                    .queryType(QueryType.SHOW)
                    .columns(columns)
                    .rows(rows)
                    .rowCount(rows.size())
                    .build();
        });
    }

    /**
     * 执行 DESCRIBE 类查询 —— CSV 格式（NULL → "NULL"）
     * <p>
     * 参照 Python 版：DESCRIBE 结果中的 NULL 值显示为字符串 "NULL"。
     * </p>
     */
    private QueryResult executeDescribe(String sql) {
        return jdbcTemplate.query(sql, (ResultSet rs) -> {
            return buildSelectResult(rs, QueryType.DESCRIBE, true);
        });
    }

    /**
     * 执行 SELECT 查询 —— CSV 格式（NULL → 空字符串）
     * <p>
     * 参照 Python 版：SELECT 结果中的 NULL 值显示为空字符串。
     * </p>
     */
    private QueryResult executeSelect(String sql) {
        return jdbcTemplate.query(sql, (ResultSet rs) -> {
            return buildSelectResult(rs, QueryType.SELECT, false);
        });
    }

    /**
     * 构建 SELECT/DESCRIBE 的 ResultSet 结果
     *
     * @param rs         结果集
     * @param queryType  查询类型
     * @param nullAsNull 是否将 NULL 显示为 "NULL"（true=DESCRIBE模式，false=SELECT模式）
     */
    private QueryResult buildSelectResult(ResultSet rs, QueryType queryType, boolean nullAsNull) throws java.sql.SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();

        List<String> columns = new ArrayList<>();
        for (int i = 1; i <= colCount; i++) {
            columns.add(meta.getColumnName(i));
        }

        // 限制返回行数以防内存溢出
        int maxRows = 10000;
        List<String[]> rows = new ArrayList<>();
        int rowIdx = 0;
        while (rs.next() && rowIdx < maxRows) {
            String[] row = new String[colCount];
            for (int i = 0; i < colCount; i++) {
                String val = rs.getString(i + 1);
                if (val == null) {
                    // DESCRIBE 模式：NULL → "NULL"，SELECT 模式：NULL → ""
                    row[i] = nullAsNull ? "NULL" : "";
                } else {
                    row[i] = val;
                }
            }
            rows.add(row);
            rowIdx++;
        }

        if (rowIdx >= maxRows) {
            log.warn("SQL执行_结果集超过上限, maxRows={}", maxRows);
        }

        log.info("SQL执行_查询结果, rows={}, cols={}", rows.size(), colCount);
        return QueryResult.builder()
                .queryType(queryType)
                .columns(columns)
                .rows(rows)
                .rowCount(rows.size())
                .build();
    }

    /**
     * 执行 DML 语句 —— 返回影响行数
     * <p>
     * 参照 Python 版：INSERT/UPDATE/DELETE/DDL 等无 ResultSet 的语句，
     * 返回 "Rows affected: N" 或 "Query OK"。
     * </p>
     */
    private QueryResult executeDml(String sql) {
        int affected = jdbcTemplate.update(sql);

        log.info("SQL执行_DML结果, affected={}", affected);
        return QueryResult.builder()
                .queryType(QueryType.DML)
                .rowCount(affected)
                .build();
    }
}
