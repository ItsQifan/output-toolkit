package com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 传输模式配置
 * <p>
 * 映射 MCP_TRANSPORT / MCP_SSE_* 环境变量。
 * 支持 STDIO 和 SSE 两种传输模式。
 * </p>
 *
 * @author qifan
 * @since 2026-07-09
 */
@Data
@Component
@ConfigurationProperties(prefix = "mcp")
public class TransportConfig {

    /** 传输模式：stdio / sse，默认 stdio */
    private String transport = "stdio";

    /** SSE 模式下的服务端口，默认 8000 */
    private int ssePort = 8000;

    /** SSE 模式下的消息端点路径 */
    private String sseMessageEndpoint = "/messages/";

    /** SSE 允许的 Host 列表，逗号分隔（DNS 重绑定保护） */
    private String sseAllowedHosts = "localhost,127.0.0.1";
}
