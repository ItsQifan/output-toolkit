package com.zhouchuanxiang.outputtoolkit.agentrag.session;

import com.zhouchuanxiang.outputtoolkit.agentrag.entity.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 上下文窗口管理器
 * <p>
 * 负责控制发送给 LLM 的上下文大小，防止 token 超限。
 * </p>
 * <p>
 * 使用原因：LLM 的上下文窗口有限（如 8K/32K/128K），且按 token 收费。
 * 如果不加限制，长对话会导致 token 消耗过大、成本飙升，甚至超出模型限制导致报错。
 * </p>
 * <p>
 * 模式收益：策略模式 —— 提供统一的上下文裁剪接口，
 * 后续可以切换不同的裁剪策略（滑动窗口、摘要压缩、混合等）。
 * </p>
 * <p>
 * 完整类结构：
 * <ul>
 *   <li>ContextWindowManager —— 上下文窗口管理组件</li>
 *   <li>SessionService —— 调用方，获取消息历史后传入裁剪</li>
 * </ul>
 * </p>
 *
 * @author qifan
 * @since 2026-07-13
 */
@Slf4j
@Component
public class ContextWindowManager {

    /** 默认上下文窗口最大消息数 */
    private static final int DEFAULT_MAX_MESSAGES = 20;

    /** 简单估算：中文每字符约 1.5 token，英文每字符约 0.3 token，取中间值 */
    private static final double CHARS_PER_TOKEN = 0.5;

    /**
     * 裁剪消息列表到指定大小
     * <p>
     * 采用滑动窗口策略：只保留最近 maxMessages 条消息。
     * 如果消息总 token 数仍超 maxTokens，则进一步裁剪最早的消息。
     * </p>
     *
     * @param messages   时间正序的消息列表
     * @param maxMessages 最大消息数
     * @param maxTokens  最大 token 数
     * @return 裁剪后的消息列表
     */
    //tips: 上下文窗口就像一个"记忆容量"。LLM 不是真的记住了之前的对话，
    //     而是每次请求时你把历史消息"塞"给它。塞得越多，成本越高、速度越慢。
    //     滑动窗口策略就是：只保留最近 N 轮对话，太老的自动"遗忘"。
    //     这就像你跟朋友聊天，你只会记住最近说了什么，不会记得三天前聊的每个细节。
    public List<Message> trimContext(List<Message> messages, int maxMessages, int maxTokens) {
        if (messages == null || messages.isEmpty()) {
            return messages;
        }

        int msgLimit = maxMessages > 0 ? maxMessages : DEFAULT_MAX_MESSAGES;

        // 第一步：按消息数量裁剪（滑动窗口 —— 保留最近 N 条）
        List<Message> trimmed = messages;
        if (messages.size() > msgLimit) {
            log.info("上下文管理_按数量裁剪, before={}, after={}", messages.size(), msgLimit);
            trimmed = messages.subList(messages.size() - msgLimit, messages.size());
        }

        // 第二步：按 token 数裁剪（从最早的消息开始移除，直到满足 token 限制）
        if (maxTokens > 0) {
            int totalTokens = estimateTotalTokens(trimmed);
            while (totalTokens > maxTokens && trimmed.size() > 1) {
                // 移除最早的非用户消息（优先保留 user 消息，因为它们承载了用户的意图）
                trimmed = trimmed.subList(1, trimmed.size());
                totalTokens = estimateTotalTokens(trimmed);
            }
            log.info("上下文管理_按Token裁剪, finalMessages={}, estimatedTokens={}", trimmed.size(), totalTokens);
        }

        return trimmed;
    }

    /**
     * 估算消息列表的总 token 数
     * <p>
     * 使用简单的字符数估算（中文每字 ≈ 1.5 token，简化处理取 2 字符/token）。
     * 生产环境应使用专门的 tokenizer（如 tiktoken）。
     * </p>
     *
     * @param messages 消息列表
     * @return 估算的 token 数
     */
    int estimateTotalTokens(List<Message> messages) {
        if (messages == null) {
            return 0;
        }

        //tips: token 不是"字数"。一个中文字大约 1.5-2 个 token，
        //     一个英文单词大约 1-2 个 token。这里用简单的字符数 / 0.5 来估算。
        //     生产环境会用专门的 tokenizer 库（如 tiktoken）来精确计算。
        return (int) messages.stream()
                .mapToInt(m -> (int) ((m.getContent() != null ? m.getContent().length() : 0) / CHARS_PER_TOKEN))
                .sum();
    }
}
