package com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.service;

import com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.config.DbConfig;
import com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.db.QueryResult;
import com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.db.QueryResult.QueryType;
import com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.security.IdentifierValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

/**
 * Schema 信息查询服务
 * <p>
 * 负责查询表的列信息（列名、类型、可空、默认值、注释），
 * 通过查询 information_schema.COLUMNS 实现，支持跨库 database.table 格式。
 * </p>
 *
 * @author qifan
 * @since 2026-07-09
 */
@Slf4j
@Service
public class SchemaInfoService {

    private final JdbcTemplate jdbcTemplate;
    private final DbConfig dbConfig;
    private final IdentifierValidator identifierValidator;

    public SchemaInfoService(JdbcTemplate jdbcTemplate,
                             DbConfig dbConfig,
                             IdentifierValidator identifierValidator) {
        this.jdbcTemplate = jdbcTemplate;
        this.dbConfig = dbConfig;
        this.identifierValidator = identifierValidator;
    }

    /**
     * 获取表的 Schema 信息
     * <p>
     * 查询 information_schema.COLUMNS 获取列元数据：
     * 表名、列名、数据类型、是否可空、默认值、列注释。
     * </p>
     *
     * @param tableName 表名，可为 null（查询所有表），支持 database.table 跨库格式
     * @return Schema 信息查询结果
     */
    public QueryResult getSchemaInfo(String tableName) {
        // 解析跨库格式 database.table
        String database;
        String table;
        if (tableName != null && !tableName.isEmpty()) {
            String[] parts = identifierValidator.parseTableArg(tableName);
            database = parts[0];
            table = parts[1];
        } else {
            database = null;
            table = null;
        }

        // 如果未指定数据库，使用配置的默认数据库
        if (database == null || database.isEmpty()) {
            database = dbConfig.getDatabase();
        }

        // 构建 information_schema 查询 SQL
        String sql = buildSchemaQuery(database, table);
        log.info("Schema查询_执行SQL, database={}, table={}", database, table);

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
                    String val = rs.getString(i + 1);
                    // DESCRIBE 风格：NULL → "NULL"
                    row[i] = (val != null) ? val : "NULL";
                }
                rows.add(row);
            }

            log.info("Schema查询_结果, rows={}", rows.size());
            return QueryResult.builder()
                    .queryType(QueryType.DESCRIBE)
                    .columns(columns)
                    .rows(rows)
                    .rowCount(rows.size())
                    .build();
        });
    }

    /**
     * 构建 information_schema 查询 SQL
     * <p>
     * 查询 COLUMNS 表获取：表名、列名、数据类型、是否可空、默认值、列注释。
     * </p>
     *
     * @param database 数据库名，为 null 时查询所有库（通过 LIKE 排除系统库）
     * @param table    表名，为 null 时查询所有表
     * @return SQL 语句
     */
    private String buildSchemaQuery(String database, String table) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ")
                .append("TABLE_SCHEMA, ")
                .append("TABLE_NAME, ")
                .append("COLUMN_NAME, ")
                .append("COLUMN_TYPE, ")
                .append("IS_NULLABLE, ")
                .append("COLUMN_DEFAULT, ")
                .append("COLUMN_COMMENT, ")
                .append("ORDINAL_POSITION ")
                .append("FROM information_schema.COLUMNS ")
                .append("WHERE 1=1 ");

        if (database != null && !database.isEmpty()) {
            sql.append("AND TABLE_SCHEMA = '").append(database).append("' ");
        } else {
            // 多库模式：排除 MySQL 系统库
            sql.append("AND TABLE_SCHEMA NOT IN ")
                    .append("('information_schema', 'mysql', 'performance_schema', 'sys') ");
        }

        if (table != null && !table.isEmpty()) {
            sql.append("AND TABLE_NAME = '").append(table).append("' ");
        }

        sql.append("ORDER BY TABLE_SCHEMA, TABLE_NAME, ORDINAL_POSITION");

        return sql.toString();
    }
}
