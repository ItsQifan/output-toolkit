package com.zhouchuanxiang.outputtoolkit.agentrag.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 订单查询工具（Spring AI 原生 Tool Calling）
 * <p>
 * 查询模拟业务订单表（t_order），支持按时间范围、商品分类、客户姓名筛选，
 * 以及简单的聚合统计（按分类汇总销售额）。
 * </p>
 * <p>
 * 使用原因：需要一个"真实的业务工具"来展示 Agent 的 Function Calling 能力。
 * 订单查询是常见的业务场景，能直观展示 Agent 如何将自然语言问题转换为结构化查询。
 * </p>
 * <p>
 * 模式收益：将数据库查询逻辑封装为独立工具，Agent 无需知道 SQL 细节，
 * 只需传入筛选条件即可获得格式化的查询结果。
 * </p>
 *
 * @author qifan
 * @since 2026-07-13
 */
//tips: 用户说"查一下最近一个月的订单"，Agent 的执行流程：
//     1. LLM 理解意图 → 需要查订单，需要知道"最近一个月"的时间范围
//     2. LLM 先调用 get_current_time 获取当前日期，算出时间范围
//     3. LLM 调用 query_orders，传入 dateFrom/dateTo 参数
//     4. Spring AI 框架自动执行本方法，把结果回传给 LLM
//     5. LLM 阅读结果，生成自然语言回答
//     整个 tool_calls 解析、参数绑定、结果回传都由框架完成，代码里看不到一行 JSON 解析。
@Slf4j
@Component
public class OrderQueryTool {

    /** 默认返回条数上限 */
    private static final int DEFAULT_LIMIT = 20;

    /** 最大返回条数上限（防止大结果集撑爆 LLM 上下文窗口） */
    private static final int MAX_LIMIT = 50;

    private final JdbcTemplate jdbcTemplate;

    public OrderQueryTool(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 查询订单数据
     *
     * @param dateFrom     查询开始日期（可选）
     * @param dateTo       查询结束日期（可选）
     * @param category     商品分类（可选）
     * @param customerName 客户姓名（可选）
     * @param aggregate    是否按分类汇总（可选）
     * @param limit        返回条数上限（可选）
     * @param toolContext  Spring AI 工具上下文（框架注入，不暴露给 LLM）
     * @return 格式化的查询结果（自然语言描述，供 LLM 阅读）
     */
    @Tool(name = "query_orders", description = """
            查询模拟业务订单数据。支持以下查询方式：
            1. 按时间范围筛选（dateFrom, dateTo）
            2. 按商品分类筛选（category）
            3. 按客户姓名筛选（customerName）
            4. 按分类汇总销售额（aggregate=true 时返回各分类的总销售额）
            当用户询问订单、销售、客户等相关问题时调用此工具。
            """)
    public String queryOrders(
            @ToolParam(description = "查询开始日期，格式 yyyy-MM-dd，如 2026-07-01", required = false) String dateFrom,
            @ToolParam(description = "查询结束日期，格式 yyyy-MM-dd，如 2026-07-13", required = false) String dateTo,
            @ToolParam(description = "商品分类：电子产品/办公家具/运动户外/图书教育", required = false) String category,
            @ToolParam(description = "客户姓名", required = false) String customerName,
            @ToolParam(description = "是否按分类汇总销售额（true=汇总, false=明细）", required = false) Boolean aggregate,
            @ToolParam(description = "返回条数上限，默认20，最大50", required = false) Integer limit,
            ToolContext toolContext) {
        // 推送工具调用事件到前端推理链
        DateTimeTool.ToolEventSupport.notifyToolCall(toolContext, "query_orders");

        log.info("订单查询_开始查询, dateFrom={}, dateTo={}, category={}, customerName={}, aggregate={}",
                dateFrom, dateTo, category, customerName, aggregate);

        if (Boolean.TRUE.equals(aggregate)) {
            return queryAggregated(dateFrom, dateTo);
        }
        return queryDetails(dateFrom, dateTo, category, customerName, normalizeLimit(limit));
    }

    /**
     * 查询订单明细
     */
    private String queryDetails(String dateFrom, String dateTo, String category, String customerName, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT order_no, product_name, category, quantity, unit_price, total_amount,
                       customer_name, status, created_at
                FROM t_order WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();

        // 动态构建查询条件（参数化查询，避免 SQL 注入）
        if (dateFrom != null && !dateFrom.isBlank()) {
            sql.append(" AND created_at >= ?");
            params.add(dateFrom + " 00:00:00");
        }
        if (dateTo != null && !dateTo.isBlank()) {
            sql.append(" AND created_at <= ?");
            params.add(dateTo + " 23:59:59");
        }
        if (category != null && !category.isBlank()) {
            sql.append(" AND category = ?");
            params.add(category);
        }
        if (customerName != null && !customerName.isBlank()) {
            sql.append(" AND customer_name = ?");
            params.add(customerName);
        }

        sql.append(" ORDER BY created_at DESC LIMIT ?");
        params.add(limit);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        return formatDetailResults(rows, limit);
    }

    /**
     * 按分类汇总查询
     */
    private String queryAggregated(String dateFrom, String dateTo) {
        StringBuilder sql = new StringBuilder("""
                SELECT category, COUNT(*) AS order_count,
                       SUM(total_amount) AS total_sales,
                       AVG(total_amount) AS avg_sales
                FROM t_order WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();

        if (dateFrom != null && !dateFrom.isBlank()) {
            sql.append(" AND created_at >= ?");
            params.add(dateFrom + " 00:00:00");
        }
        if (dateTo != null && !dateTo.isBlank()) {
            sql.append(" AND created_at <= ?");
            params.add(dateTo + " 23:59:59");
        }

        sql.append(" GROUP BY category ORDER BY total_sales DESC");

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        return formatAggregatedResults(rows);
    }

    /**
     * 格式化明细查询结果为自然语言
     */
    private String formatDetailResults(List<Map<String, Object>> rows, int limit) {
        if (rows.isEmpty()) {
            return "未找到符合条件的订单记录。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("找到 ").append(rows.size()).append(" 条订单记录");
        if (rows.size() >= limit) {
            sb.append("（已达查询上限，可能存在更多记录）");
        }
        sb.append("：\n\n");

        for (int i = 0; i < rows.size(); i++) {
            Map<String, Object> row = rows.get(i);
            sb.append(i + 1).append(". ")
                    .append("订单号：").append(row.get("order_no")).append(" | ")
                    .append("商品：").append(row.get("product_name")).append(" | ")
                    .append("分类：").append(row.get("category")).append(" | ")
                    .append("数量：").append(row.get("quantity")).append(" | ")
                    .append("金额：¥").append(row.get("total_amount")).append(" | ")
                    .append("客户：").append(row.get("customer_name")).append(" | ")
                    .append("状态：").append(row.get("status")).append(" | ")
                    .append("时间：").append(row.get("created_at"))
                    .append("\n");
        }
        return sb.toString();
    }

    /**
     * 格式化汇总查询结果为自然语言
     */
    private String formatAggregatedResults(List<Map<String, Object>> rows) {
        if (rows.isEmpty()) {
            return "未找到符合条件的订单记录，无法进行汇总统计。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("按分类汇总的销售统计：\n\n");

        for (int i = 0; i < rows.size(); i++) {
            Map<String, Object> row = rows.get(i);
            sb.append(i + 1).append(". ")
                    .append("分类：").append(row.get("category")).append(" | ")
                    .append("订单数：").append(row.get("order_count")).append(" | ")
                    .append("总销售额：¥").append(row.get("total_sales")).append(" | ")
                    .append("平均客单价：¥").append(row.get("avg_sales"))
                    .append("\n");
        }
        return sb.toString();
    }

    /**
     * 归一化 limit 参数（默认20，范围 1~50）
     */
    private int normalizeLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        return Math.min(Math.max(limit, 1), MAX_LIMIT);
    }
}
