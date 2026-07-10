package com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.mcp;

import com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.db.QueryResult;
import com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.db.QueryResult.QueryType;
import com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.security.IdentifierValidator;
import com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.service.QueryExecutionService;
import com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.service.SchemaInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MCP 工具定义 —— 提供 3 个 MCP 工具
 * <p>
 * 对应 Python 版 mysql_mcp_server 的 3 个工具：
 * <ul>
 *   <li><b>execute_sql</b>：执行 SQL 语句（读写）</li>
 *   <li><b>get_schema_info</b>：查询表结构信息（只读）</li>
 *   <li><b>get_table_sample</b>：获取表数据样本（只读）</li>
 * </ul>
 * <p>
 * 使用原因：将 MCP 工具定义集中管理，便于注册和维护。
 * <p>
 * 模式收益：单一职责 —— 每个方法对应一个 MCP 工具，职责清晰。
 * <p>
 * 完整类结构：
 * <ul>
 *   <li>McpToolDefinitions —— 工具定义类，包含 3 个 @Tool 方法</li>
 *   <li>QueryExecutionService —— SQL 执行服务，被 executeSql / getTableSample 调用</li>
 *   <li>SchemaInfoService —— Schema 查询服务，被 getSchemaInfo 调用</li>
 *   <li>IdentifierValidator —— 标识符校验，被 getSchemaInfo / getTableSample 调用</li>
 * </ul>
 *
 * @author qifan
 * @since 2026-07-09
 */
@Slf4j
@Component
public class McpToolDefinitions {

    private final QueryExecutionService queryExecutionService;
    private final SchemaInfoService schemaInfoService;
    private final IdentifierValidator identifierValidator;

    public McpToolDefinitions(QueryExecutionService queryExecutionService,
                              SchemaInfoService schemaInfoService,
                              IdentifierValidator identifierValidator) {
        this.queryExecutionService = queryExecutionService;
        this.schemaInfoService = schemaInfoService;
        this.identifierValidator = identifierValidator;
    }

    /**
     * 执行 SQL 语句
     * <p>
     * 支持 SELECT、INSERT、UPDATE、DELETE、SHOW、DESCRIBE 等各类 SQL 语句。
     * 单语句执行，不支持多条语句（多语句会被拒绝）。
     * </p>
     *
     * @param query 待执行的 SQL 语句
     * @return 格式化后的查询结果文本
     */
    @McpTool(description = "执行 SQL 语句。支持 SELECT / INSERT / UPDATE / DELETE / SHOW / DESCRIBE 等。仅支持单条语句，不支持多语句执行。")
    public String executeSql(
            @McpToolParam(description = "待执行的 SQL 语句，仅支持单条 SQL") String query) {
        try {
            log.info("SQL执行_收到 execute_sql 请求, sql={}", query);
            QueryResult result = queryExecutionService.execute(query);
            return formatQueryResult(result);
        } catch (Exception e) {
            log.error("SQL执行_执行失败, sql={}", query, e);
            return "Error calling tool execute_sql: " + e.getMessage();
        }
    }

    /**
     * 获取表的 Schema 信息（列名、类型、可空、默认值、注释）
     * <p>
     * 查询 information_schema.COLUMNS，支持 database.table 跨库格式。
     * 不传 tableName 时返回所有表的列信息。
     * </p>
     *
     * @param tableName 表名（可选），支持 database.table 格式
     * @return Schema 信息文本
     */
    @McpTool(description = "获取表的列信息（列名、数据类型、是否可空、默认值、注释）。不传表名则返回所有表的列信息。支持 database.table 跨库格式。")
    public String getSchemaInfo(
            @McpToolParam(description = "表名（可选），支持 database.table 跨库格式，不传则查询所有表")
            String tableName) {
        try {
            log.info("Schema查询_收到 get_schema_info 请求, tableName={}", tableName);

            // 如果指定了表名，先校验标识符合法性（防注入）
            if (tableName != null && !tableName.isEmpty()) {
                identifierValidator.parseTableArg(tableName);
            }

            QueryResult result = schemaInfoService.getSchemaInfo(tableName);
            return formatQueryResult(result);
        } catch (Exception e) {
            log.error("Schema查询_查询失败, tableName={}", tableName, e);
            return "Error calling tool get_schema_info: " + e.getMessage();
        }
    }

    /**
     * 获取表的数据样本
     * <p>
     * 执行 SELECT * FROM {table} LIMIT {n} 获取前 N 行数据。
     * </p>
     *
     * @param tableName 表名（必填），支持 database.table 格式
     * @param limit     返回行数，默认 5，最大 20
     * @return CSV 格式的样本数据
     */
    @McpTool(description = "获取表的样本数据（前 N 行）。执行 SELECT * FROM {table} LIMIT {n}。limit 默认 5，最大 20。支持 database.table 跨库格式。")
    public String getTableSample(
            @McpToolParam(description = "表名（必填），支持 database.table 跨库格式") String tableName,
            @McpToolParam(description = "返回行数，默认 5，最大 20") Integer limit) {
        try {
            // 参数校验与规范化
            if (tableName == null || tableName.isEmpty()) {
                return "Error calling tool get_table_sample: table_name is required";
            }

            // 限制 limit 范围：默认 5，最大 20
            int actualLimit = (limit == null || limit <= 0) ? 5 : Math.min(limit, 20);

            // 校验标识符合法性（防注入）
            // parseTableArg 会校验 database 和 table 两部分
            String[] parts = identifierValidator.parseTableArg(tableName);
            String fullTableName = parts[0] != null
                    ? "`" + parts[0] + "`.`" + parts[1] + "`"
                    : "`" + parts[1] + "`";

            // 生成查询 SQL：SELECT * FROM {table} LIMIT {n}
            String sql = "SELECT * FROM " + fullTableName + " LIMIT " + actualLimit;

            log.info("样本查询_执行SQL, tableName={}, limit={}, sql={}", tableName, actualLimit, sql);

            QueryResult result = queryExecutionService.execute(sql);
            return formatQueryResult(result);
        } catch (Exception e) {
            log.error("样本查询_查询失败, tableName={}, limit={}", tableName, limit, e);
            return "Error calling tool get_table_sample: " + e.getMessage();
        }
    }

    /**
     * 格式化 QueryResult 为文本输出
     * <p>
     * 四种输出格式：
     * <ul>
     *   <li>SHOW → 列名行 + 数据行，用换行分隔</li>
     *   <li>DESCRIBE → CSV 格式（逗号分隔），NULL → "NULL"</li>
     *   <li>SELECT → CSV 格式（逗号分隔），NULL → ""</li>
     *   <li>DML → "Rows affected: N"</li>
     * </ul>
     * </p>
     *
     * @param result 查询结果
     * @return 格式化文本
     */
    private String formatQueryResult(QueryResult result) {
        if (result == null) {
            return "Query returned no result.";
        }

        QueryType type = result.getQueryType();
        if (type == QueryType.DML) {
            return "Rows affected: " + result.getRowCount();
        }

        // SHOW / DESCRIBE / SELECT → 列名 + 数据行
        List<String> columns = result.getColumns();
        List<String[]> rows = result.getRows();

        if (columns == null || columns.isEmpty()) {
            return "Query returned empty result.";
        }

        StringBuilder sb = new StringBuilder();

        // 列名行
        sb.append(String.join(",", columns)).append("\n");

        // 数据行
        if (rows != null) {
            for (String[] row : rows) {
                for (int i = 0; i < row.length; i++) {
                    if (i > 0) {
                        sb.append(",");
                    }
                    // CSV 值中包含逗号、换行或引号时用引号包裹
                    String val = row[i] != null ? row[i] : "";
                    if (val.contains(",") || val.contains("\n") || val.contains("\"")) {
                        sb.append("\"").append(val.replace("\"", "\"\"")).append("\"");
                    } else {
                        sb.append(val);
                    }
                }
                sb.append("\n");
            }
        }

        sb.append("Row count: ").append(result.getRowCount());
        return sb.toString();
    }
}
