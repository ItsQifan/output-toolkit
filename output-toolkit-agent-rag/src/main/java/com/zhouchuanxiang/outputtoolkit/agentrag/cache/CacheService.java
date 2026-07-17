package com.zhouchuanxiang.outputtoolkit.agentrag.cache;

import cn.hutool.crypto.SecureUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 问答两级缓存服务（基于 Redis）
 * <p>
 * 缓存层次设计：
 * <ul>
 *   <li>L1 热点问答缓存：问题归一化后精确匹配，命中直接返回完整答案，
 *       跳过 RAG 检索 + LLM 调用整条链路（响应从秒级降到毫秒级）</li>
 *   <li>L2 检索结果缓存：缓存向量检索的 TopK 结果，命中跳过
 *       Embedding 计算与 Milvus 查询，答案仍由 LLM 实时生成</li>
 * </ul>
 * </p>
 * <p>
 * 失效策略：知识库变更（文档入库完成/删除）时全量清空两级缓存，
 * 保证缓存内容不会引用已过期的知识；同时 TTL 兜底防止长期脏数据。
 * </p>
 *
 * @author qifan
 * @since 2026-07-16
 */
//tips: 为什么要"两级"缓存而不是只缓存最终答案？
//     L1 只有"一字不差的重复问题"才命中，命中率有限但收益最大（省掉整条链路）；
//     L2 覆盖"表述相近但检索词一致"的场景，虽然 LLM 还要调用，
//     但省掉了 Embedding API 调用和 Milvus 检索的开销与费用。
//     两级组合让"完全重复"和"部分重复"的请求都能受益。
@Slf4j
@Service
public class CacheService {

    /** L1 热点问答缓存 Key 前缀 */
    private static final String ANSWER_KEY_PREFIX = "agent-rag:answer:";

    /** L2 检索结果缓存 Key 前缀 */
    private static final String RETRIEVAL_KEY_PREFIX = "agent-rag:retrieve:";

    /** 缓存 Key 扫描批次大小（SCAN 命令每批返回数量） */
    private static final long SCAN_BATCH_SIZE = 500L;

    private final StringRedisTemplate redisTemplate;

    /** L1 答案缓存过期时间（秒） */
    private final long answerTtlSeconds;

    /** L2 检索缓存过期时间（秒） */
    private final long retrievalTtlSeconds;

    public CacheService(StringRedisTemplate redisTemplate,
                        @Value("${agent-rag.cache.answer-ttl-seconds}") long answerTtlSeconds,
                        @Value("${agent-rag.cache.retrieval-ttl-seconds}") long retrievalTtlSeconds) {
        this.redisTemplate = redisTemplate;
        this.answerTtlSeconds = answerTtlSeconds;
        this.retrievalTtlSeconds = retrievalTtlSeconds;
    }

    /**
     * 查询 L1 热点问答缓存
     *
     * @param question 用户原始问题
     * @return 缓存的答案，未命中返回 null
     */
    public String getCachedAnswer(String question) {
        try {
            String answer = redisTemplate.opsForValue().get(ANSWER_KEY_PREFIX + normalizeKey(question));
            if (answer != null) {
                log.info("问答缓存_L1命中, question={}", abbreviate(question));
            }
            return answer;
        } catch (Exception e) {
            // Redis 故障时降级为不使用缓存，不影响主链路
            log.warn("问答缓存_L1查询失败已降级, question={}, error={}", abbreviate(question), e.getMessage());
            return null;
        }
    }

    /**
     * 写入 L1 热点问答缓存
     *
     * @param question 用户原始问题
     * @param answer   LLM 生成的最终答案
     */
    public void cacheAnswer(String question, String answer) {
        try {
            redisTemplate.opsForValue().set(ANSWER_KEY_PREFIX + normalizeKey(question), answer,
                    Duration.ofSeconds(answerTtlSeconds));
        } catch (Exception e) {
            log.warn("问答缓存_L1写入失败已忽略, question={}, error={}", abbreviate(question), e.getMessage());
        }
    }

    /**
     * 查询 L2 检索结果缓存
     *
     * @param question 用户原始问题
     * @return 缓存的检索结果 JSON，未命中返回 null
     */
    public String getCachedRetrieval(String question) {
        try {
            String json = redisTemplate.opsForValue().get(RETRIEVAL_KEY_PREFIX + normalizeKey(question));
            if (json != null) {
                log.info("问答缓存_L2命中, question={}", abbreviate(question));
            }
            return json;
        } catch (Exception e) {
            log.warn("问答缓存_L2查询失败已降级, question={}, error={}", abbreviate(question), e.getMessage());
            return null;
        }
    }

    /**
     * 写入 L2 检索结果缓存
     *
     * @param question      用户原始问题
     * @param retrievalJson 检索结果 JSON 字符串
     */
    public void cacheRetrieval(String question, String retrievalJson) {
        try {
            redisTemplate.opsForValue().set(RETRIEVAL_KEY_PREFIX + normalizeKey(question), retrievalJson,
                    Duration.ofSeconds(retrievalTtlSeconds));
        } catch (Exception e) {
            log.warn("问答缓存_L2写入失败已忽略, question={}, error={}", abbreviate(question), e.getMessage());
        }
    }

    /**
     * 清空两级缓存（知识库变更时调用）
     * <p>
     * 使用 SCAN 渐进式遍历替代 KEYS 命令：KEYS 会一次性阻塞 Redis 主线程，
     * 生产环境大 Key 量时会造成服务卡顿，SCAN 分批游标遍历则无此问题。
     * </p>
     */
    public void evictAll() {
        try {
            long removed = evictByPrefix(ANSWER_KEY_PREFIX) + evictByPrefix(RETRIEVAL_KEY_PREFIX);
            log.info("问答缓存_知识库变更清空缓存, removedKeys={}", removed);
        } catch (Exception e) {
            log.warn("问答缓存_清空失败已忽略, error={}", e.getMessage());
        }
    }

    /**
     * 按前缀渐进式删除缓存 Key
     *
     * @param prefix Key 前缀
     * @return 删除的 Key 数量
     */
    private long evictByPrefix(String prefix) {
        List<String> keys = new ArrayList<>();
        ScanOptions options = ScanOptions.scanOptions().match(prefix + "*").count(SCAN_BATCH_SIZE).build();
        // try-with-resources 确保游标关闭，避免连接泄漏
        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            cursor.forEachRemaining(keys::add);
        }
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        return keys.size();
    }

    /**
     * 问题归一化生成缓存 Key
     * <p>
     * 归一化规则：去除所有空白字符和常见标点 + 转小写，再取 MD5。
     * 让"如何申请风控白名单？"和"如何申请风控白名单"这类形近问题命中同一缓存。
     * </p>
     *
     * @param question 用户原始问题
     * @return 归一化后的 MD5 摘要
     */
    private String normalizeKey(String question) {
        String normalized = question
                .replaceAll("[\\s，。？！、；：,.?!;:]+", "")
                .toLowerCase();
        return SecureUtil.md5(normalized);
    }

    /**
     * 截断长问题用于日志输出
     */
    private String abbreviate(String text) {
        return text != null && text.length() > 50 ? text.substring(0, 50) + "..." : text;
    }
}
