package com.zhouchuanxiang.outputtoolkit.agentrag.exception;

/**
 * Agent 执行异常
 * <p>
 * 用于表示 Agent 执行流程中的错误（如 LLM 调用失败、工具执行失败等）。
 * </p>
 *
 * @author qifan
 * @since 2026-07-13
 */
public class AgentException extends RuntimeException {

    public AgentException(String message) {
        super(message);
    }

    public AgentException(String message, Throwable cause) {
        super(message, cause);
    }
}
