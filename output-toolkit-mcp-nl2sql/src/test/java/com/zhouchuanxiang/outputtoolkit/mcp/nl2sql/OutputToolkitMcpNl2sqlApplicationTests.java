package com.zhouchuanxiang.outputtoolkit.mcp.nl2sql;

import com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.config.DbConfig;
import com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.config.TransportConfig;
import com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.mcp.McpToolDefinitions;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MySQL MCP Server 集成测试
 * <p>
 * Phase 1：验证 Spring 容器启动、配置加载、MCP 工具注册。
 * </p>
 *
 * @author qifan
 * @since 2026-07-09
 */
@SpringBootTest
class OutputToolkitMcpNl2sqlApplicationTests {

    @Resource
    private DbConfig dbConfig;

    @Resource
    private TransportConfig transportConfig;

    @Resource
    private McpToolDefinitions mcpToolDefinitions;

    /**
     * 测试 Spring 容器启动
     */
    @Test
    @DisplayName("应用上下文加载测试")
    void contextLoads() {
        assertNotNull(dbConfig, "DbConfig 应被自动注入");
        assertNotNull(transportConfig, "TransportConfig 应被自动注入");
        assertNotNull(mcpToolDefinitions, "McpToolDefinitions 应被自动注入");
    }

    /**
     * 测试配置默认值加载
     */
    @Test
    @DisplayName("配置默认值测试")
    void testDefaultConfig() {
        assertEquals("localhost", dbConfig.getHost(), "默认 host 应为 localhost");
        assertEquals(3306, dbConfig.getPort(), "默认 port 应为 3306");
        assertEquals("stdio", transportConfig.getTransport(), "默认传输模式应为 stdio");
        assertEquals("DISABLED", dbConfig.getSsl().getMode(), "默认 SSL 模式应为 DISABLED");
    }

    /**
     * 测试 MCP 工具 stub 返回值
     */
    @Test
    @DisplayName("MCP 工具 stub 测试")
    void testMcpToolStubs() {
        String sqlResult = mcpToolDefinitions.executeSql("SELECT 1");
        assertNotNull(sqlResult, "executeSql 不应返回 null");
        assertTrue(sqlResult.contains("Not implemented yet"), "Phase 1 stub 应返回未实现提示");

        String schemaResult = mcpToolDefinitions.getSchemaInfo("users");
        assertNotNull(schemaResult, "getSchemaInfo 不应返回 null");

        String sampleResult = mcpToolDefinitions.getTableSample("users", 5);
        assertNotNull(sampleResult, "getTableSample 不应返回 null");
    }
}
