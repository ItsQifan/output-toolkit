package com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.db;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * SQL 查询结果封装
 * <p>
 * 统一封装不同类型 SQL 的查询结果：
 * <ul>
 *   <li>SELECT 查询 → 列名 + 行数据（CSV 格式）</li>
 *   <li>SHOW/DESCRIBE → 带表头列表</li>
 *   <li>DML 语句 → 影响行数</li>
 * </ul>
 * </p>
 *
 * @author qifan
 * @since 2026-07-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryResult {

    /** 查询类型 */
    private QueryType queryType;

    /** 列名列表（SELECT 结果集时使用） */
    private List<String> columns;

    /** 数据行（每行为一个 String 数组） */
    private List<String[]> rows;

    /** 影响行数（DML 语句时使用） */
    private int rowCount;

    /**
     * 查询类型枚举
     */
    public enum QueryType {
        /** SELECT 查询，返回结果集 */
        SELECT,

        /** SHOW TABLES / SHOW DATABASES 等元数据查询 */
        SHOW,

        /** DESCRIBE / SHOW COLUMNS 等表结构查询 */
        DESCRIBE,

        /** INSERT / UPDATE / DELETE / DDL 等无结果集语句 */
        DML
    }
}
