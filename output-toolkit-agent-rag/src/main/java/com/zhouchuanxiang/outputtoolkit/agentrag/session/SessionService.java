package com.zhouchuanxiang.outputtoolkit.agentrag.session;

import com.zhouchuanxiang.outputtoolkit.agentrag.entity.Conversation;
import com.zhouchuanxiang.outputtoolkit.agentrag.entity.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 会话管理服务
 * <p>
 * 负责会话的创建、查询、删除以及消息的持久化。
 * 使用原因：将会话和消息的 CRUD 逻辑封装到一个服务中，Controller 只需关心业务调度。
 * </p>
 * <p>
 * 模式收益：Facade 模式 —— 对外提供简洁的会话操作接口，内部协调 ConversationMapper 和 MessageMapper。
 * </p>
 * <p>
 * 完整类结构：
 * <ul>
 *   <li>SessionService —— 会话管理门面</li>
 *   <li>ConversationMapper —— 会话数据访问</li>
 *   <li>MessageMapper —— 消息数据访问</li>
 *   <li>ContextWindowManager —— 上下文窗口管理</li>
 * </ul>
 * </p>
 *
 * @author qifan
 * @since 2026-07-13
 */
@Slf4j
@Service
public class SessionService {

    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;

    public SessionService(ConversationMapper conversationMapper,
                          MessageMapper messageMapper) {
        this.conversationMapper = conversationMapper;
        this.messageMapper = messageMapper;
    }

    /**
     * 创建新会话
     *
     * @param userId 用户标识
     * @param title  会话标题
     * @return 新创建的会话对象（包含自增ID）
     */
    public Conversation createConversation(String userId, String title) {
        Conversation conversation = Conversation.builder()
                .userId(userId)
                .title(title != null && !title.isBlank() ? title : "新对话")
                .status(1)
                .build();
        conversationMapper.insert(conversation);
        log.info("会话管理_创建会话成功, conversationId={}, userId={}", conversation.getId(), userId);
        return conversation;
    }

    /**
     * 获取或创建会话
     * <p>
     * 如果传入的 conversationId 有效则返回已有会话，否则创建新会话。
     * 用于聊天接口：客户端首次请求不传 conversationId，后续请求携带。
     * </p>
     *
     * @param conversationId 会话ID（可为 null）
     * @param userId         用户标识
     * @return 会话对象
     */
    public Conversation getOrCreateConversation(Long conversationId, String userId) {
        if (conversationId != null && conversationId > 0) {
            Conversation existing = conversationMapper.selectById(conversationId);
            if (existing != null) {
                return existing;
            }
            log.warn("会话管理_会话不存在，将创建新会话, conversationId={}", conversationId);
        }
        return createConversation(userId, "新对话");
    }

    /**
     * 查询用户的所有会话列表
     *
     * @param userId 用户标识
     * @return 会话列表（按更新时间倒序）
     */
    public List<Conversation> listConversations(String userId) {
        return conversationMapper.selectByUserId(userId);
    }

    /**
     * 查询会话详情
     *
     * @param conversationId 会话ID
     * @return 会话对象，不存在返回 null
     */
    public Conversation getConversation(Long conversationId) {
        return conversationMapper.selectById(conversationId);
    }

    /**
     * 保存用户消息
     *
     * @param conversationId 会话ID
     * @param content        消息内容
     */
    public void saveUserMessage(Long conversationId, String content) {
        Message message = Message.builder()
                .conversationId(conversationId)
                .role("user")
                .content(content)
                .build();
        messageMapper.insert(message);

        // 首次对话时自动用首条问题作为标题
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation != null && "新对话".equals(conversation.getTitle())) {
            String title = content.length() > 30 ? content.substring(0, 30) : content;
            conversationMapper.updateTitle(conversationId, title);
        }
        log.info("会话管理_保存用户消息, conversationId={}, content={}", conversationId,
                content.length() > 50 ? content.substring(0, 50) + "..." : content);
    }

    /**
     * 保存助手回答
     *
     * @param conversationId 会话ID
     * @param content        回答内容
     * @param tokenCount     token 消耗数
     */
    public void saveAssistantMessage(Long conversationId, String content, Integer tokenCount) {
        Message message = Message.builder()
                .conversationId(conversationId)
                .role("assistant")
                .content(content)
                .tokenCount(tokenCount)
                .build();
        messageMapper.insert(message);
        log.info("会话管理_保存助手回答, conversationId={}, tokenCount={}", conversationId, tokenCount);
    }

    /**
     * 保存工具调用记录
     *
     * @param conversationId 会话ID
     * @param toolName       工具名称
     * @param content        工具执行结果
     */
    public void saveToolMessage(Long conversationId, String toolName, String content) {
        Message message = Message.builder()
                .conversationId(conversationId)
                .role("tool")
                .toolName(toolName)
                .content(content)
                .build();
        messageMapper.insert(message);
        log.info("会话管理_保存工具调用记录, conversationId={}, toolName={}", conversationId, toolName);
    }

    /**
     * 获取会话的历史消息（最近 N 条）
     *
     * @param conversationId 会话ID
     * @param maxMessages    最大消息数
     * @return 时间正序的消息列表
     */
    public List<Message> getRecentMessages(Long conversationId, int maxMessages) {
        return messageMapper.selectRecentByConversationId(conversationId, maxMessages);
    }

    /**
     * 获取会话的所有历史消息
     *
     * @param conversationId 会话ID
     * @return 时间正序的消息列表
     */
    public List<Message> getAllMessages(Long conversationId) {
        return messageMapper.selectByConversationId(conversationId);
    }

    /**
     * 删除会话及其所有消息
     *
     * @param conversationId 会话ID
     */
    @Transactional
    public void deleteConversation(Long conversationId) {
        messageMapper.deleteByConversationId(conversationId);
        conversationMapper.deleteById(conversationId);
        log.info("会话管理_删除会话及消息, conversationId={}", conversationId);
    }
}
