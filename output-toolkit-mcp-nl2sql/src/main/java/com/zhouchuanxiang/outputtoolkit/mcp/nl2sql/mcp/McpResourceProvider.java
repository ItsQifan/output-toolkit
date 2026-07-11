package com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.mcp;

import com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.config.DbConfig;
import com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.security.IdentifierValidator;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * MCP 资源提供器 —— 提供数据库资源的列表和读取
 * <p>
 * 对应 Python 版 mysql_mcp_server 的资源（Resources）功能：
 * <ul>
 *   <li><b>list_resources</b>：列出可用的数据库资源</li>
 *   <li><b>read_resource</b>：读取指定资源的内容</li>
 * </ul>
 * <p>
 * 资源 URI 格式：
 * <ul>
 *   <li>单库模式：mysql://{table}/data → SELECT * LIMIT 100</li>
 *   <li>多库模式：mysql://database/{db} → 列出该库的表</li>
 * </ul>
 *
 * @author qifan
 * @since 2026-07-09
 */
@Slf4j
@Component
public class McpResourceProvider {

    /** 系统数据库，多库模式下列表时排除 */
    private static final List<String> SYSTEM_DATABASES = List.of(
            "information_schema", "mysql", "performance_schema", "sys"
    );

    private final JdbcTemplate jdbcTemplate;
    private final DbConfig dbConfig;
    private final IdentifierValidator identifierValidator;

    public McpResourceProvider(JdbcTemplate jdbcTemplate,
                               DbConfig dbConfig,
                               IdentifierValidator identifierValidator) {
        this.jdbcTemplate = jdbcTemplate;
        this.dbConfig = dbConfig;
        this.identifierValidator = identifierValidator;
    }

    /**
     * 列出所有可用的数据库资源
     * <p>
     * 单库模式 → SHOW TABLES，返回 mysql://{table}/data 格式的资源列表。
     * 多库模式 → SHOW DATABASES，过滤系统库，返回 mysql://database/{db} 格式。
     * </p>
     *
     * @return 资源 URI 列表
     */
    @McpTool(description = "列出所有可用的数据库资源。单库模式返回表列表（mysql://{table}/data），多库模式返回数据库列表（mysql://database/{db}），已过滤系统库。")
    public String listResources() {
        try {
            log.info("资源列表_收到 list_resources 请求");

            String database = dbConfig.getDatabase();
            if (database != null && !database.isEmpty()) {
                // 单库模式：SHOW TABLES
                return listTables(database);
            } else {
                // 多库模式：SHOW DATABASES，过滤系统库
                return listDatabases();
            }
        } catch (Exception e) {
            log.error("资源列表_查询失败", e);
            return "Error calling tool list_resources: " + e.getMessage();
        }
    }

    /**
     * 读取指定资源的内容
     * <p>
     * 支持两种 URI 格式：
     * <ul>
     *   <li>mysql://{table}/data → SELECT * FROM {table} LIMIT 100</li>
     *   <li>mysql://database/{db} → SHOW TABLES FROM {db}</li>
     * </ul>
     * </p>
     *
     * @param uri 资源 URI
     * @return CSV 格式的数据内容
     */
    @McpTool(description = "读取指定数据库资源的内容。URI 格式：mysql://{table}/data（表数据）或 mysql://database/{db}（库表列表）。")
    public String readResource(
            @McpToolParam(description = "资源 URI，格式：mysql://{table}/data 或 mysql://database/{db}") String uri) {
        try {
            log.info("资源读取_收到 read_resource 请求, uri={}", uri);

            if (uri == null || uri.isEmpty()) {
                return "Error: URI is required";
            }

            // 解析 URI：mysql://{name}/data 或 mysql://database/{name}
            String prefix = "mysql://";
            if (!uri.startsWith(prefix)) {
                return "Error: Invalid URI format. Expected mysql://{table}/data or mysql://database/{db}";
            }

            String path = uri.substring(prefix.length());

            if (path.startsWith("database/")) {
                // 多库模式：列出指定库的表
                String db = path.substring("database/".length());
                identifierValidator.validate(db);
                return listTables(db);
            } else if (path.endsWith("/data")) {
                // 单表模式：查询表数据
                String table = path.substring(0, path.length() - "/data".length());
                identifierValidator.validate(table);
                return queryTableData(table);
            } else {
                return "Error: Invalid URI format. Expected mysql://{table}/data or mysql://database/{db}";
            }
        } catch (Exception e) {
            log.error("资源读取_读取失败, uri={}", uri, e);
            return "Error calling tool read_resource: " + e.getMessage();
        }
    }

    /**
     * 列出指定数据库的所有表
     */
    private String listTables(String database) {
        String sql = "SHOW TABLES FROM `" + database + "`";
        List<String> tables = jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getString(1));

        StringBuilder sb = new StringBuilder();
        for (String table : tables) {
            sb.append("mysql://").append(table).append("/data\n");
        }

        log.info("资源列表_表列表, database={}, count={}", database, tables.size());
        return sb.isEmpty() ? "No tables found in database: " + database : sb.toString().trim();
    }

    /**
     * 列出所有用户数据库（排除系统库）
     */
    private String listDatabases() {
        List<String> databases = jdbcTemplate.query("SHOW DATABASES",
                (rs, rowNum) -> rs.getString(1));

        List<String> filtered = new ArrayList<>();
        for (String db : databases) {
            if (!SYSTEM_DATABASES.contains(db.toLowerCase())) {
                filtered.add(db);
            }
        }

        StringBuilder sb = new StringBuilder();
        for (String db : filtered) {
            sb.append("mysql://database/").append(db).append("\n");
        }

        log.info("资源列表_数据库列表, count={}", filtered.size());
        return sb.isEmpty() ? "No user databases found." : sb.toString().trim();
    }

    /**
     * 查询表数据（SELECT * LIMIT 100）
     */
    private String queryTableData(String table) {
        String sql = "SELECT * FROM `" + table + "` LIMIT 100";
        return jdbcTemplate.query(sql, (rs) -> {
            StringBuilder sb = new StringBuilder();
            int colCount = rs.getMetaData().getColumnCount();

            // 列名行
            for (int i = 1; i <= colCount; i++) {
                if (i > 1) sb.append(",");
                sb.append(rs.getMetaData().getColumnName(i));
            }
            sb.append("\n");

            // 数据行
            int rowCount = 0;
            while (rs.next()) {
                for (int i = 1; i <= colCount; i++) {
                    if (i > 1) sb.append(",");
                    String val = rs.getString(i);
                    sb.append(val != null ? val : "");
                }
                sb.append("\n");
                rowCount++;
            }

            log.info("资源读取_表数据, table={}, rows={}", table, rowCount);
            sb.append("Row count: ").append(rowCount);
            return sb.toString();
        });
    }
}
