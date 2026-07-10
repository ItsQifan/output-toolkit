package com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * SQL 标识符白名单校验器
 * <p>
 * 对表名、数据库名等标识符进行安全校验，防止 SQL 注入。
 * 合法标识符格式：仅包含字母、数字、下划线、美元符号。
 * </p>
 * <p>
 * 使用原因：Python 版 mysql_mcp_server 中对 get_schema_info / get_table_sample
 * 的 table_name 参数使用正则 ^[a-zA-Z0-9_$]+$ 做白名单校验，防止注入攻击。
 * Java 版使用相同策略。
 * </p>
 *
 * @author qifan
 * @since 2026-07-09
 */
@Slf4j
@Component
public class IdentifierValidator {

    /** 合法标识符正则：仅允许字母、数字、下划线、美元符号 */
    private static final String VALID_PATTERN = "^[a-zA-Z0-9_$]+$";

    /**
     * 校验标识符是否合法
     * <p>
     * 使用正则白名单 {@code ^[a-zA-Z0-9_$]+$} 校验，
     * 不合法时抛出 {@link InvalidIdentifierException}。
     * </p>
     *
     * @param identifier 待校验的标识符（表名或数据库名）
     * @return 校验通过的原标识符
     * @throws InvalidIdentifierException 标识符包含非法字符时抛出
     */
    public String validate(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            throw new InvalidIdentifierException(identifier != null ? identifier : "null");
        }

        if (!identifier.matches(VALID_PATTERN)) {
            log.warn("标识符校验_拒绝非法标识符: {}", identifier);
            throw new InvalidIdentifierException(identifier);
        }

        log.debug("标识符校验_通过: {}", identifier);
        return identifier;
    }

    /**
     * 解析 database.table 格式的跨库表名
     * <p>
     * 将 "database.table" 格式拆分为 (database, table)，
     * 并分别对两部分进行白名单校验。
     * 不支持三段式（database.table.column）——只支持 database.table。
     * </p>
     *
     * @param tableArg 表名参数，支持 "table" 和 "database.table" 两种格式
     * @return 长度为 2 的数组 [database, table]，单表时 database 为 null
     * @throws InvalidIdentifierException 任意部分包含非法字符时抛出
     */
    public String[] parseTableArg(String tableArg) {
        if (tableArg == null || tableArg.isEmpty()) {
            return new String[]{null, null};
        }

        // 按 . 拆分，最多拆成两段（database.table）
        String[] parts = tableArg.split("\\.", 2);

        if (parts.length == 1) {
            // 单表名格式：table
            return new String[]{null, validate(parts[0])};
        }

        // 跨库格式：database.table —— 分别校验
        String database = validate(parts[0]);
        String table = validate(parts[1]);
        return new String[]{database, table};
    }
}
