package com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.transport;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * SSE 传输模式配置
 * <p>
 * 当 spring.ai.mcp.server.protocol=SSE 时激活，提供：
 * <ul>
 *   <li>GET / → 健康检查端点</li>
 *   <li>DNS 重绑定保护 —— 只允许白名单内的 Host 头访问</li>
 *   <li>Spring AI 自动配置 SSE 连接端点 /sse 和消息端点</li>
 * </ul>
 * <p>
 * 参照 Python 版 uvicorn + starlette 的 SSE 实现。
 * </p>
 *
 * @author qifan
 * @since 2026-07-09
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.ai.mcp.server.protocol", havingValue = "SSE")
public class SseModeConfig {

    /**
     * DNS 重绑定保护的允许 Host 列表（逗号分隔）
     * 默认只允许 localhost 和 127.0.0.1
     */
    @Value("${mcp.sse-allowed-hosts:localhost,127.0.0.1}")
    private String allowedHosts;

    /**
     * DNS 重绑定保护过滤器
     * <p>
     * SSE 模式下面临 DNS 重绑定攻击风险：攻击者通过恶意域名解析到内网 IP，
     * 绕过同源策略攻击本服务。此过滤器校验请求的 Host 头是否在白名单内，
     * 不在白名单则返回 403 Forbidden。
     * </p>
     */
    @Bean
    public Filter dnsRebindingProtectionFilter() {
        return new Filter() {
            private Set<String> allowedHostSet;

            @Override
            public void init(FilterConfig filterConfig) {
                allowedHostSet = new HashSet<>();
                if (allowedHosts != null && !allowedHosts.isEmpty()) {
                    // 允许的 Host 列表，同时添加带端口和不带端口的版本
                    for (String host : allowedHosts.split(",")) {
                        String trimmed = host.trim();
                        if (!trimmed.isEmpty()) {
                            allowedHostSet.add(trimmed);
                        }
                    }
                }
                log.info("SSE模式_DNS重绑定保护已启用, allowedHosts={}", allowedHostSet);
            }

            @Override
            public void doFilter(ServletRequest request, ServletResponse response,
                                 FilterChain chain) throws IOException, ServletException {
                HttpServletRequest httpRequest = (HttpServletRequest) request;

                // 健康检查端点不校验 Host
                String path = httpRequest.getRequestURI();
                if ("/".equals(path) || "/health".equals(path)) {
                    chain.doFilter(request, response);
                    return;
                }

                // 校验 Host 头
                String host = httpRequest.getHeader("Host");
                if (host != null && !allowedHostSet.isEmpty()) {
                    // 去掉端口号后匹配
                    String hostWithoutPort = host.contains(":") ? host.substring(0, host.indexOf(':')) : host;
                    if (!allowedHostSet.contains(host) && !allowedHostSet.contains(hostWithoutPort)) {
                        log.warn("SSE模式_DNS重绑定拦截, host={}", host);
                        HttpServletResponse httpResponse = (HttpServletResponse) response;
                        httpResponse.setStatus(403);
                        httpResponse.getWriter().write("Forbidden: Host not allowed");
                        return;
                    }
                }

                chain.doFilter(request, response);
            }
        };
    }

    /**
     * 健康检查控制器
     * <p>
     * 提供 GET / 和 GET /health 端点，用于检测服务是否正常运行。
     * 对应 Python 版 starlette 的 health check。
     * </p>
     */
    @RestController
    public static class HealthController {

        @GetMapping("/")
        public ResponseEntity<String> root() {
            return ResponseEntity.ok("MySQL MCP Server is running (SSE mode)");
        }

        @GetMapping("/health")
        public ResponseEntity<String> health() {
            return ResponseEntity.ok("OK");
        }
    }
}
