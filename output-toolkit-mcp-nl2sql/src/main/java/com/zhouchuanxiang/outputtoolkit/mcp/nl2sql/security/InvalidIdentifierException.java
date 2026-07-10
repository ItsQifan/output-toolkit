package com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.security;

/**
 * 非法标识符异常
 * <p>
 * 当数据库表名、库名等标识符包含非法字符（如 SQL 注入字符）时抛出。
 * 合法标识符仅允许：字母、数字、下划线、美元符号。
 * </p>
 *
 * @author qifan
 * @since 2026-07-10
 */
public class InvalidIdentifierException extends RuntimeException {

    /** 触发异常的非法标识符 */
    private final String identifier;

    /**
     * 构造非法标识符异常
     *
     * @param identifier 被拒绝的标识符值
     */
    public InvalidIdentifierException(String identifier) {
        super("非法标识符: '" + identifier + "' —— 仅允许字母、数字、下划线和美元符号");
        this.identifier = identifier;
    }

    /**
     * 获取触发异常的标识符
     *
     * @return 被拒绝的标识符值
     */
    public String getIdentifier() {
        return identifier;
    }
}
