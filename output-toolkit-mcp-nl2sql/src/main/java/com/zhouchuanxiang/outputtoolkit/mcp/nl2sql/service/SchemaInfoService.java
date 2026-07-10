package com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.service;

import com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.db.QueryResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Schema 信息查询服务
 * <p>
 * 负责查询表的列信息（列名、类型、可空、默认值、注释），
 * 支持 information_schema 查询和跨库 table 格式（database.table）。
 * </p>
 *
 * @author qifan
 * @since 2026-07-09
 */
@Slf4j
@Service
public class SchemaInfoService {

    /**
     * 获取表的 Schema 信息
     * <p>
     * Phase 1 为 stub 实现，抛出 UnsupportedOperationException。
     * Phase 4 实现完整 Schema 查询逻辑。
     * </p>
     *
     * @param tableName 表名，可为 null（查询所有表），支持 database.table 跨库格式
     * @return Schema 信息查询结果
     */
    public QueryResult getSchemaInfo(String tableName) {
        // TODO Phase 4: 生成 information_schema 查询 SQL 并执行
        throw new UnsupportedOperationException("Phase 4 实现：Schema 信息服务尚未就绪");
    }
}
