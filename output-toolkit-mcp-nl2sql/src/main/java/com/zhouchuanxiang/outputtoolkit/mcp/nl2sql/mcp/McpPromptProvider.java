package com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.mcp;

import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

/**
 * MCP 提示词提供器 —— 提供 2 个数据库探索提示词模板
 * <p>
 * 对应 Python 版 mysql_mcp_server 的 2 个提示词（Prompts）：
 * <ul>
 *   <li><b>explore_database</b>：4 步数据库探索指导</li>
 *   <li><b>analyze_table</b>：3 步表分析指导</li>
 * </ul>
 * <p>
 * 使用原因：为 AI 客户端提供结构化的数据库探索和分析指导模板。
 *
 * @author qifan
 * @since 2026-07-09
 */
@Slf4j
@Component
public class McpPromptProvider {

    /**
     * 数据库探索提示词
     * <p>
     * 引导 AI 按照 4 个步骤系统性地探索数据库：
     * <ol>
     *   <li>使用 list_resources 了解数据库中有哪些表</li>
     *   <li>使用 get_schema_info 查看关键表的结构</li>
     *   <li>使用 get_table_sample 获取样本数据理解内容</li>
     *   <li>使用 execute_sql 执行具体查询</li>
     * </ol>
     * </p>
     *
     * @return 探索指导文本
     */
    @McpTool(description = "获取数据库探索指导 —— 提供系统性的 4 步数据库探索流程")
    public String exploreDatabase() {
        // Phase 1 stub：返回固定描述文本
        return """
                数据库探索指导（4 步）：
                1. 使用 list_resources 了解数据库中有哪些表
                2. 使用 get_schema_info 查看关键表的结构（列名、类型、注释）
                3. 使用 get_table_sample 获取样本数据以理解数据内容和模式
                4. 使用 execute_sql 执行具体查询以回答用户问题
                """;
    }

    /**
     * 表分析提示词
     * <p>
     * 引导 AI 按照 3 个步骤分析指定表：
     * <ol>
     *   <li>获取表的完整 Schema 信息</li>
     *   <li>查看样本数据了解数据分布</li>
     *   <li>执行聚合查询获取统计信息</li>
     * </ol>
     * </p>
     *
     * @param tableName 要分析的表名
     * @return 分析指导文本
     */
    @McpTool(description = "获取表分析指导 —— 提供针对特定表的 3 步分析流程")
    public String analyzeTable(
            @McpToolParam(description = "要分析的表名") String tableName) {
        // Phase 1 stub：返回固定描述文本
        return String.format("""
                        表 '%s' 分析指导（3 步）：
                        1. 使用 get_schema_info("%s") 获取表的完整列信息
                        2. 使用 get_table_sample("%s") 查看样本数据，了解数据分布和内容
                        3. 使用 execute_sql 执行聚合查询（COUNT、GROUP BY 等）获取统计信息
                        """,
                tableName, tableName, tableName);
    }
}
