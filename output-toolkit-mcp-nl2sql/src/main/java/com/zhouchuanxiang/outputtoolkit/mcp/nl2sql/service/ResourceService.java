package com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 资源管理服务
 * <p>
 * 负责 MCP 资源（Resources）的列表和读取：
 * <ul>
 *   <li>单库模式 → SHOW TABLES，返回 mysql://{table}/data</li>
 *   <li>多库模式 → SHOW DATABASES，过滤系统库，返回 mysql://database/{db}</li>
 * </ul>
 * </p>
 *
 * @author qifan
 * @since 2026-07-09
 */
@Slf4j
@Service
public class ResourceService {

    /**
     * 列出所有可用资源
     * <p>
     * Phase 1 为 stub 实现，返回空列表。
     * Phase 5 实现完整资源列表逻辑。
     * </p>
     *
     * @return 资源 URI 列表
     */
    public List<Map<String, String>> listResources() {
        // TODO Phase 5: 根据单库/多库模式查询表或数据库列表
        return Collections.emptyList();
    }

    /**
     * 读取指定资源的内容
     * <p>
     * Phase 1 为 stub 实现，抛出 UnsupportedOperationException。
     * Phase 5 实现完整资源读取逻辑。
     * </p>
     *
     * @param uri 资源 URI（mysql://{table}/data 或 mysql://database/{db}）
     * @return 资源内容字符串
     */
    public String readResource(String uri) {
        // TODO Phase 5: 解析 URI 并执行对应查询
        throw new UnsupportedOperationException("Phase 5 实现：资源读取服务尚未就绪");
    }
}
