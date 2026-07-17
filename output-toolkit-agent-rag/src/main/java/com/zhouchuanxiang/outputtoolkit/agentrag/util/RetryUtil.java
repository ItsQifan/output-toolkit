package com.zhouchuanxiang.outputtoolkit.agentrag.util;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

/**
 * 超时重试工具
 * <p>
 * 提供指数退避重试机制，用于 LLM 调用、网络请求等可能出现瞬时错误的场景。
 * </p>
 * <p>
 * 指数退避（Exponential Backoff）意味着每次重试的等待时间翻倍：
 * 第1次重试等1秒，第2次等2秒，第3次等4秒...给服务端恢复的时间。
 * </p>
 *
 * @author qifan
 * @since 2026-07-13
 */
@Slf4j
public class RetryUtil {

    /** 默认最大重试次数 */
    private static final int DEFAULT_MAX_RETRIES = 3;

    /** 默认初始退避延迟（毫秒） */
    private static final long DEFAULT_INITIAL_DELAY_MS = 1000;

    /** 退避倍增因子（每次延迟 ×2） */
    private static final double BACKOFF_MULTIPLIER = 2.0;

    private RetryUtil() {
        // 工具类，禁止实例化
    }

    /**
     * 带重试的执行（使用默认参数：3次重试，1秒初始延迟）
     *
     * @param supplier    需要重试的操作
     * @param description 操作描述（用于日志）
     * @param <T>         返回值类型
     * @return 操作结果
     * @throws RuntimeException 所有重试耗尽后抛出
     */
    //tips: 为什么 LLM 调用需要重试？大模型 API 偶尔会"抽风"——
    //     网络超时、服务繁忙限流、临时故障...这些都不是代码bug，
    //     等几秒再试一次可能就恢复了。指数退避就是每次等更久，
    //     给服务器喘息的时间，避免"越忙越压"导致雪崩。
    public static <T> T executeWithRetry(Supplier<T> supplier, String description) {
        return executeWithRetry(supplier, DEFAULT_MAX_RETRIES, DEFAULT_INITIAL_DELAY_MS, description);
    }

    /**
     * 带重试的执行（自定义参数）
     *
     * @param supplier      需要重试的操作
     * @param maxRetries    最大重试次数
     * @param initialDelayMs 初始退避延迟（毫秒）
     * @param description   操作描述（用于日志）
     * @param <T>           返回值类型
     * @return 操作结果
     * @throws RuntimeException 所有重试耗尽后抛出
     */
    public static <T> T executeWithRetry(Supplier<T> supplier, int maxRetries, long initialDelayMs, String description) {
        long delay = initialDelayMs;
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                if (attempt > 1) {
                    log.info("重试机制_{}——第{}次重试（共{}次），等待{}ms后执行",
                            description, attempt - 1, maxRetries - 1, delay);
                }
                return supplier.get();
            } catch (Exception e) {
                lastException = e;
                log.warn("重试机制_{}——第{}次执行失败, error={}", description, attempt, e.getMessage());

                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("重试被中断", ie);
                    }
                    // 指数退避：每次延迟翻倍
                    delay = (long) (delay * BACKOFF_MULTIPLIER);
                }
            }
        }

        // 所有重试耗尽
        String errorMsg = String.format("重试机制_%s——全部%d次重试已耗尽", description, maxRetries);
        log.error(errorMsg, lastException);
        throw new RuntimeException(errorMsg, lastException);
    }
}
