package com.zhouchuanxiang.outputtoolkit.agentrag.exception;

/**
 * RAG 业务异常
 * <p>
 * 用于表示 RAG 处理流程中的可恢复错误（如文档解析失败、向量化失败等）。
 * 与 AgentException 区分：RagException 侧重数据层问题，AgentException 侧重执行层问题。
 * </p>
 *
 * @author qifan
 * @since 2026-07-13
 */
public class RagException extends RuntimeException {

    public RagException(String message) {
        super(message);
    }

    public RagException(String message, Throwable cause) {
        super(message, cause);
    }
}
