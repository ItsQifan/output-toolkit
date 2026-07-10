package com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.mcp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

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

    /**
     * 列出所有可用的数据库资源
     * <p>
     * 单库模式 → SHOW TABLES，返回 mysql://{table}/data。
     * 多库模式 → SHOW DATABASES，过滤系统库，返回 mysql://database/{db}。
     * </p>
     *
     * @return 资源 URI 列表（JSON 格式）
     */
    @Tool(description = "列出所有可用的数据库资源（表或数据库）。单库模式返回表列表，多库模式返回数据库列表（已过滤系统库）。")
    public String listResources() {
        // TODO Phase 5: 调用 ResourceService.listResources() 并格式化返回
        log.info("资源列表_收到 list_resources 请求");
        return "[]";
    }

    /**
     * 读取指定资源的内容
     * <p>
     * 解析 URI 并执行对应查询。
     * </p>
     *
     * @param uri 资源 URI
     * @return 资源内容
     */
    @Tool(description = "读取指定资源的内容。支持 mysql://{table}/data（表数据）和 mysql://database/{db}（库表列表）两种格式。")
    public String readResource(
            @ToolParam(description = "资源 URI，格式：mysql://{table}/data 或 mysql://database/{db}") String uri) {
        // TODO Phase 5: 解析 URI 并调用 ResourceService.readResource(uri)
        log.info("资源读取_收到 read_resource 请求, uri={}", uri);
        return "Not implemented yet — Phase 5 实现";
    }
}
