package com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.transport;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * STDIO 传输模式配置
 * <p>
 * 当 spring.ai.mcp.server.stdio=true 时激活（默认），
 * 使用标准输入输出与 MCP 客户端（如 Claude Desktop）通信。
 * </p>
 * <p>
 * Spring AI 自动处理 STDIO 模式的 MCP 协议通信，无需额外配置。
 * 本地使用 Claude Desktop 时推荐此模式。
 * </p>
 *
 * @author qifan
 * @since 2026-07-09
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.ai.mcp.server.stdio", havingValue = "true", matchIfMissing = true)
public class StdioModeConfig {

    // Spring AI 自动处理 STDIO 传输，无需额外 Bean
    // MCP 工具/资源/提示词通过 @McpTool 注解自动注册
}
