package com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.transport;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * SSE 传输模式配置
 * <p>
 * 当 mcp.transport=sse 时激活，提供 HTTP SSE 端点供 MCP 客户端连接。
 * 包含 DNS 重绑定保护 —— 只允许配置的 Host 头访问。
 * </p>
 *
 * @author qifan
 * @since 2026-07-09
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "mcp.transport", havingValue = "sse")
public class SseModeConfig {

    // Phase 1: 骨架占位，Phase 6 完善 SSE 传输配置
    // - GET / → 健康检查
    // - GET /sse → SSE 连接端点
    // - POST /messages/ → 消息端点
    // - DNS 重绑定保护
}
