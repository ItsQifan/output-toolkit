package com.zhouchuanxiang.outputtoolkit.mcp.nl2sql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * MySQL MCP Server —— 主启动类
 * <p>
 * 基于 MCP (Model Context Protocol) 协议的 MySQL 数据库查询服务，
 * 提供 execute_sql / get_schema_info / get_table_sample 三个 MCP 工具，
 * 支持 STDIO / SSE 双传输模式、SSH 隧道、SSL 加密连接。
 * </p>
 * <p>
 * 使用原因：Java 17 + Spring AI + Spring Boot 重构 Python 版 mysql_mcp_server，
 * 提供更好的性能和可维护性。
 * </p>
 * <p>
 * 模式收益：
 * <ul>
 *   <li>Spring Boot 自动配置：简化数据库连接、MCP Server 配置</li>
 *   <li>Spring AI MCP Starter：开箱即用的 MCP 协议支持</li>
 *   <li>HikariCP 连接池：高性能数据库连接管理</li>
 * </ul>
 * </p>
 *
 * @author qifan
 * @since 2026-07-09
 */
@SpringBootApplication
@EnableConfigurationProperties
public class OutputToolkitMcpNl2sqlApplication {

    public static void main(String[] args) {
        SpringApplication.run(OutputToolkitMcpNl2sqlApplication.class, args);
    }
}
