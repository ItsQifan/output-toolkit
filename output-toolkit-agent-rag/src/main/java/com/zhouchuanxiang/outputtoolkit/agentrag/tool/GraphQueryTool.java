package com.zhouchuanxiang.outputtoolkit.agentrag.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 风控图谱关系查询工具（Spring AI 原生 Tool Calling）
 * <p>
 * 模拟风控图谱关系系统的查询接口：以实体（用户/设备/商户/IP）为起点，
 * 查询其关联的实体和关系类型，支持多度关系展开（如"用户→设备→其他用户"的团伙挖掘）。
 * 数据存储在 MySQL 的 t_graph_entity / t_graph_relation 两张表中。
 * </p>
 * <p>
 * 使用原因：风控问答场景中，"用户 U1001 关联了哪些设备和 IP"这类问题
 * 无法通过静态知识库文档回答，必须实时查询图谱关系系统。
 * 通过 Tool Calling 将图谱查询能力暴露给 LLM，实现问答过程中的动态数据调用。
 * </p>
 *
 * @author qifan
 * @since 2026-07-16
 */
//tips: 风控图谱的核心思想是"关系即风险"：
//     一个设备被 10 个用户登录过 → 可能是猫池设备（批量注册养号）；
//     多个用户共用一个 IP 且在同一商户交易 → 可能是团伙套现。
//     图谱查询工具让 LLM 能在回答问题时实时"顺藤摸瓜"，
//     这是 RAG 静态知识检索无法覆盖的动态数据场景，两者互补。
@Slf4j
@Component
public class GraphQueryTool {

    /** 单实体关系查询的最大返回条数（防止大结果集撑爆 LLM 上下文） */
    private static final int MAX_RELATIONS_PER_ENTITY = 30;

    /** 关系展开的最大深度（防止图遍历爆炸） */
    private static final int MAX_DEPTH = 2;

    private final JdbcTemplate jdbcTemplate;

    public GraphQueryTool(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 查询实体的图谱关联关系
     *
     * @param entityName  实体名称
     * @param depth       关系展开深度（可选，默认1）
     * @param toolContext Spring AI 工具上下文（框架注入，不暴露给 LLM）
     * @return 格式化的关系描述（自然语言，供 LLM 阅读）
     */
    @Tool(name = "query_graph_relations", description = """
            查询风控图谱关系系统。以指定实体（用户/设备/商户/IP）为起点，查询其关联的实体和关系。
            支持的实体名称格式如：用户U1001、设备D2001、商户M3001、IP:192.168.1.100。
            depth=1 查询直接关联，depth=2 查询二度关联（如通过共用设备找到的关联用户，用于团伙分析）。
            当用户询问某个用户/设备/商户/IP 的关联关系、风险关系、团伙关系时调用此工具。
            """)
    public String queryGraphRelations(
            @ToolParam(description = "实体名称，如 用户U1001、设备D2001、商户M3001、IP:192.168.1.100") String entityName,
            @ToolParam(description = "关系展开深度，1=直接关联（默认），2=二度关联", required = false) Integer depth,
            ToolContext toolContext) {
        // 推送工具调用事件到前端推理链
        DateTimeTool.ToolEventSupport.notifyToolCall(toolContext, "query_graph_relations");

        int expandDepth = depth != null ? Math.min(Math.max(depth, 1), MAX_DEPTH) : 1;
        log.info("图谱查询_开始查询, entityName={}, depth={}", entityName, expandDepth);

        // 实体存在性校验，不存在时给 LLM 明确反馈（而非空结果误导）
        Map<String, Object> entity = findEntity(entityName);
        if (entity == null) {
            return "图谱中不存在实体「" + entityName + "」，请确认实体名称格式（如：用户U1001、设备D2001、商户M3001、IP:192.168.1.100）。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("实体「").append(entityName).append("」（类型：").append(entity.get("entity_type")).append("）")
                .append(entity.get("description") != null ? "，" + entity.get("description") : "")
                .append("\n\n");

        // BFS 逐层展开关系：第一层是直接关联，第二层用于团伙/传导分析
        Set<String> visited = new LinkedHashSet<>();
        visited.add(entityName);
        List<String> currentLayer = List.of(entityName);

        for (int level = 1; level <= expandDepth; level++) {
            List<String> nextLayer = new ArrayList<>();
            sb.append("【").append(level == 1 ? "直接关联" : level + "度关联").append("】\n");
            boolean hasRelation = false;

            for (String current : currentLayer) {
                List<Map<String, Object>> relations = findRelations(current);
                for (Map<String, Object> rel : relations) {
                    String source = (String) rel.get("source_entity");
                    String target = (String) rel.get("target_entity");
                    // 找出关系中"对端"实体（图是无向查询：本实体可能是 source 也可能是 target）
                    String other = current.equals(source) ? target : source;
                    if (visited.contains(other)) {
                        continue;
                    }
                    visited.add(other);
                    nextLayer.add(other);
                    hasRelation = true;
                    sb.append("- ").append(source).append(" --[").append(rel.get("relation_type")).append("]--> ")
                            .append(target)
                            .append(rel.get("description") != null ? "（" + rel.get("description") + "）" : "")
                            .append("\n");
                }
            }

            if (!hasRelation) {
                sb.append("- 无更多关联\n");
                break;
            }
            currentLayer = nextLayer;
        }

        String result = sb.toString();
        log.info("图谱查询_查询完成, entityName={}, relatedEntities={}", entityName, visited.size() - 1);
        return result;
    }

    /**
     * 查询实体信息
     *
     * @param entityName 实体名称
     * @return 实体行数据，不存在返回 null
     */
    private Map<String, Object> findEntity(String entityName) {
        String sql = """
                SELECT entity_name, entity_type, description
                FROM t_graph_entity WHERE entity_name = ? LIMIT 1
                """;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, entityName);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /**
     * 查询实体的所有直接关系（实体作为源或目标均可）
     *
     * @param entityName 实体名称
     * @return 关系列表
     */
    private List<Map<String, Object>> findRelations(String entityName) {
        String sql = """
                SELECT source_entity, target_entity, relation_type, weight, description
                FROM t_graph_relation
                WHERE source_entity = ? OR target_entity = ?
                ORDER BY weight DESC
                LIMIT ?
                """;
        return jdbcTemplate.queryForList(sql, entityName, entityName, MAX_RELATIONS_PER_ENTITY);
    }
}
