package com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.transport;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * STDIO 传输模式配置
 * <p>
 * 当 mcp.transport=stdio 时激活，使用标准输入输出与 MCP 客户端通信。
 * Spring AI MCP Server 自动配置 STDIO 模式，此处为条件化占位配置。
 * </p>
 *
 * @author qifan
 * @since 2026-07-09
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "mcp.transport", havingValue = "stdio", matchIfMissing = true)
public class StdioModeConfig {

    // Phase 1: Spring AI 自动处理 STDIO 模式，无需额外配置
    // Phase 6: 可能需要添加自定义配置
}
