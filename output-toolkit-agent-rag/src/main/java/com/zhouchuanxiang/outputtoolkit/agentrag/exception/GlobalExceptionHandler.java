package com.zhouchuanxiang.outputtoolkit.agentrag.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * 全局异常处理器
 * <p>
 * 统一处理所有 Controller 层抛出的异常，返回统一的 JSON 格式错误响应。
 * 避免异常直接暴露给前端，同时记录完整的错误日志。
 * </p>
 *
 * @author qifan
 * @since 2026-07-13
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理 RAG 业务异常
     */
    @ExceptionHandler(RagException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleRagException(RagException e) {
        log.error("全局异常_RAG异常", e);
        return Map.of("code", 400, "msg", e.getMessage());
    }

    /**
     * 处理 Agent 执行异常
     */
    @ExceptionHandler(AgentException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleAgentException(AgentException e) {
        log.error("全局异常_Agent异常", e);
        return Map.of("code", 500, "msg", "Agent执行失败：" + e.getMessage());
    }

    /**
     * 处理参数校验异常（JSR303）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        log.warn("全局异常_参数校验失败, message={}", message);
        return Map.of("code", 400, "msg", message);
    }

    /**
     * 处理其他未分类异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleUnknownException(Exception e) {
        log.error("全局异常_未知异常", e);
        return Map.of("code", 500, "msg", "服务器内部错误：" + e.getMessage());
    }
}
