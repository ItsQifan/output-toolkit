package com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * SQL 标识符白名单校验器
 * <p>
 * 对表名、数据库名等标识符进行安全校验，防止 SQL 注入。
 * 合法标识符格式：仅包含字母、数字、下划线、美元符号。
 * </p>
 *
 * @author qifan
 * @since 2026-07-09
 */
@Slf4j
@Component
public class IdentifierValidator {

    /** 合法标识符正则：字母、数字、下划线、美元符号 */
    private static final String VALID_IDENTIFIER_PATTERN = "^[a-zA-Z0-9_$]+$";

    /**
     * 校验标识符是否合法
     * <p>
     * Phase 1 为 stub 实现，直接返回原值不做校验。
     * Phase 3 实现完整白名单校验逻辑。
     * </p>
     *
     * @param identifier 待校验的标识符（表名或数据库名）
     * @return 校验通过的原标识符
     */
    public String validate(String identifier) {
        // TODO Phase 3: 实现正则校验，不合法时抛 InvalidIdentifierException
        return identifier;
    }
}
