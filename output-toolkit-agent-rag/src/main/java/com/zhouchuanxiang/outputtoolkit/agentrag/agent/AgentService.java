package com.zhouchuanxiang.outputtoolkit.agentrag.agent;

import com.alibaba.fastjson2.JSON;
import com.zhouchuanxiang.outputtoolkit.agentrag.cache.CacheService;
import com.zhouchuanxiang.outputtoolkit.agentrag.dto.ChatEvent;
import com.zhouchuanxiang.outputtoolkit.agentrag.entity.Message;
import com.zhouchuanxiang.outputtoolkit.agentrag.rag.RetrievalService;
import com.zhouchuanxiang.outputtoolkit.agentrag.session.ContextWindowManager;
import com.zhouchuanxiang.outputtoolkit.agentrag.session.SessionService;
import com.zhouchuanxiang.outputtoolkit.agentrag.tool.DateTimeTool;
import com.zhouchuanxiang.outputtoolkit.agentrag.tool.GraphQueryTool;
import com.zhouchuanxiang.outputtoolkit.agentrag.tool.OrderQueryTool;
import com.zhouchuanxiang.outputtoolkit.agentrag.util.RetryUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Agent 核心编排服务（基于 Spring AI ChatClient + 原生 Tool Calling）
 * <p>
 * 负责编排 Agent 的完整执行流程：
 * <ol>
 *   <li>L1 缓存检查：热点问答缓存命中直接返回，跳过检索与 LLM 调用</li>
 *   <li>RAG 检索（带 L2 缓存）：从 Milvus 检索与问题相关的文档片段</li>
 *   <li>构建 Prompt：System Prompt + 历史消息 + RAG 上下文</li>
 *   <li>LLM 调用：通过 ChatClient 原生 Tool Calling，框架自动完成
 *       "LLM 决策 → 执行工具 → 结果回传 → 继续推理"的多轮循环</li>
 *   <li>流式推送：通过 SSE 将推理过程实时推送给前端</li>
 *   <li>会话持久化：保存完整的对话历史到 MySQL</li>
 * </ol>
 * </p>
 * <p>
 * 改造说明：早期版本通过 Prompt 驱动方式手写 ReAct 循环（System Prompt 里约定 JSON
 * 格式 + 手动解析 LLM 文本），已替换为 Spring AI 原生 Tool Calling——
 * 工具通过 {@code @Tool} 注解声明，API 协议层传递 tools 参数，框架自动解析
 * tool_calls 并执行回传，可靠性和可维护性大幅提升。
 * </p>
 *
 * @author qifan
 * @since 2026-07-13
 */
//tips: Agent（智能体）就像一个带工具箱的AI助手。普通LLM只能聊天，
//     Agent 多了"动手能力"——它可以决定什么时候该查数据库、查图谱、查时间。
//     原生 Tool Calling 模式下，ReAct 循环（思考→行动→观察→再思考）
//     由 Spring AI 框架内部完成：chatClient.call() 一次调用背后，
//     框架可能已经和 LLM 往返了多轮（每轮执行一个工具），
//     直到 LLM 不再请求工具、产出最终答案才返回。
@Slf4j
@Service
public class AgentService {

    /** RAG 检索返回的文档片段数量 */
    private static final int RAG_TOP_K = 5;

    /** RAG 检索的最低相似度阈值 */
    private static final double RAG_THRESHOLD = 0.6;

    /** 历史消息加载条数上限 */
    private static final int HISTORY_LIMIT = 20;

    /** 上下文窗口的 Token 预算 */
    private static final int CONTEXT_TOKEN_BUDGET = 8000;

    private final ChatClient chatClient;
    private final RetrievalService retrievalService;
    private final SessionService sessionService;
    private final ContextWindowManager contextWindowManager;
    private final CacheService cacheService;
    private final DateTimeTool dateTimeTool;
    private final OrderQueryTool orderQueryTool;
    private final GraphQueryTool graphQueryTool;

    public AgentService(ChatClient chatClient,
                        RetrievalService retrievalService,
                        SessionService sessionService,
                        ContextWindowManager contextWindowManager,
                        CacheService cacheService,
                        DateTimeTool dateTimeTool,
                        OrderQueryTool orderQueryTool,
                        GraphQueryTool graphQueryTool) {
        this.chatClient = chatClient;
        this.retrievalService = retrievalService;
        this.sessionService = sessionService;
        this.contextWindowManager = contextWindowManager;
        this.cacheService = cacheService;
        this.dateTimeTool = dateTimeTool;
        this.orderQueryTool = orderQueryTool;
        this.graphQueryTool = graphQueryTool;
    }

    /**
     * 执行 Agent 对话（SSE 流式模式）
     *
     * @param conversationId 会话ID（null时创建新会话）
     * @param userId         用户标识
     * @param question       用户问题
     * @param eventConsumer  SSE事件消费者
     * @return Agent 执行结果
     */
    public AgentResult executeStream(Long conversationId, String userId, String question,
                                     Consumer<ChatEvent> eventConsumer) {
        // 1. 获取或创建会话
        var conversation = sessionService.getOrCreateConversation(conversationId, userId);

        // 2. L1 热点问答缓存检查
        //tips: 为什么只在"新会话"时使用答案缓存？因为多轮对话中同一个问题的答案
        //     依赖上下文（比如用户先说"我在说U1001"再问"它关联了哪些设备"），
        //     直接复用缓存答案会答非所问。新会话首问无上下文依赖，可安全复用。
        boolean freshConversation = conversationId == null;
        if (freshConversation) {
            String cachedAnswer = cacheService.getCachedAnswer(question);
            if (cachedAnswer != null) {
                log.info("Agent对话_L1缓存命中直接返回, conversationId={}", conversation.getId());
                sessionService.saveUserMessage(conversation.getId(), question);
                eventConsumer.accept(ChatEvent.thinking("命中热点问答缓存，直接返回"));
                eventConsumer.accept(ChatEvent.answer(cachedAnswer));
                sessionService.saveAssistantMessage(conversation.getId(), cachedAnswer, 0);
                eventConsumer.accept(ChatEvent.done(conversation.getId(), 0));
                return new AgentResult(conversation.getId(), cachedAnswer, 0);
            }
        }

        // 3. 保存用户消息
        sessionService.saveUserMessage(conversation.getId(), question);

        // 4. RAG 检索（带 L2 检索结果缓存）
        eventConsumer.accept(ChatEvent.thinking("正在检索相关文档..."));
        List<RetrievalService.RetrievalResult> ragResults = retrieveWithCache(question);
        String ragContext = buildRagContext(ragResults);
        eventConsumer.accept(ChatEvent.thinking("检索到 " + ragResults.size() + " 个相关文档片段"));

        // 5. 加载历史消息并裁剪上下文窗口
        List<Message> historyMessages = sessionService.getRecentMessages(conversation.getId(), HISTORY_LIMIT);
        List<Message> trimmedHistory = contextWindowManager.trimContext(historyMessages, HISTORY_LIMIT, CONTEXT_TOKEN_BUDGET);
        List<org.springframework.ai.chat.messages.Message> history = new ArrayList<>();
        for (Message msg : trimmedHistory) {
            switch (msg.getRole()) {
                case "user" -> history.add(new UserMessage(msg.getContent()));
                case "assistant" -> history.add(new AssistantMessage(msg.getContent()));
                default -> {
                    // system/tool 角色的历史消息不回放：system 每轮重建，tool 结果已融入 assistant 回答
                }
            }
        }

        // 6. LLM 调用（原生 Tool Calling，框架自动完成工具执行的多轮循环）
        eventConsumer.accept(ChatEvent.thinking("正在思考..."));
        ChatResponse response = RetryUtil.executeWithRetry(() -> chatClient.prompt()
                        .system(buildSystemPrompt(ragContext))
                        .messages(history)
                        .user(question)
                        // 注册工具：LLM 可按需调用时间/订单/图谱三个工具
                        .tools(dateTimeTool, orderQueryTool, graphQueryTool)
                        // 通过 ToolContext 把 SSE 事件消费者传给工具，工具执行时推送 tool_call 事件
                        .toolContext(Map.of(DateTimeTool.ToolEventSupport.EVENT_CONSUMER_KEY, eventConsumer))
                        .call()
                        .chatResponse(),
                "Agent对话_LLM调用");

        String finalAnswer = response != null && response.getResult() != null
                ? response.getResult().getOutput().getText()
                : "";
        if (finalAnswer == null || finalAnswer.isBlank()) {
            finalAnswer = "抱歉，本次未能生成有效回答，请重试或换个问法。";
        }

        // 统计 token 消耗
        int totalTokens = 0;
        if (response != null && response.getMetadata() != null && response.getMetadata().getUsage() != null) {
            totalTokens = response.getMetadata().getUsage().getTotalTokens();
        }

        // 7. 推送最终回答
        eventConsumer.accept(ChatEvent.answer(finalAnswer));
        log.info("Agent对话_回答完成, conversationId={}, tokens={}", conversation.getId(), totalTokens);

        // 8. 保存助手消息
        sessionService.saveAssistantMessage(conversation.getId(), finalAnswer, totalTokens);

        // 9. 写入 L1 热点问答缓存（仅新会话首问，理由同上）
        if (freshConversation) {
            cacheService.cacheAnswer(question, finalAnswer);
        }

        // 10. 推送完成事件
        eventConsumer.accept(ChatEvent.done(conversation.getId(), totalTokens));

        return new AgentResult(conversation.getId(), finalAnswer, totalTokens);
    }

    /**
     * 带 L2 缓存的 RAG 检索
     * <p>
     * 缓存命中时跳过 Embedding 计算与 Milvus 查询；
     * 未命中时执行真实检索并回填缓存。
     * </p>
     *
     * @param question 用户问题
     * @return 检索结果列表
     */
    private List<RetrievalService.RetrievalResult> retrieveWithCache(String question) {
        // 先查 L2 缓存
        String cachedJson = cacheService.getCachedRetrieval(question);
        if (cachedJson != null) {
            try {
                return JSON.parseArray(cachedJson, RetrievalService.RetrievalResult.class);
            } catch (Exception e) {
                // 缓存内容损坏时降级为真实检索，不影响主链路
                log.warn("RAG检索_L2缓存反序列化失败已降级, error={}", e.getMessage());
            }
        }

        // 真实检索 + 回填缓存
        List<RetrievalService.RetrievalResult> results = retrievalService.retrieve(question, RAG_TOP_K, RAG_THRESHOLD);
        cacheService.cacheRetrieval(question, JSON.toJSONString(results));
        return results;
    }

    /**
     * 构建 RAG 检索结果的上下文文本
     */
    private String buildRagContext(List<RetrievalService.RetrievalResult> results) {
        if (results == null || results.isEmpty()) {
            return "未找到相关文档资料。请根据你的通用知识回答用户问题，并告知用户当前知识库中没有相关文档。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("以下是从知识库中检索到的相关资料：\n\n");
        for (int i = 0; i < results.size(); i++) {
            var result = results.get(i);
            sb.append("【资料").append(i + 1).append("】");
            if (result.metadata() != null && result.metadata().containsKey(RetrievalService.META_DOCUMENT_NAME)) {
                sb.append(" 来源：").append(result.metadata().get(RetrievalService.META_DOCUMENT_NAME));
            }
            sb.append("\n").append(result.content()).append("\n\n");
        }
        return sb.toString();
    }

    /**
     * 构建 System Prompt
     * <p>
     * 原生 Tool Calling 模式下无需在 Prompt 里描述工具调用格式
     * （工具 Schema 由框架通过 API 的 tools 参数传递），
     * Prompt 只需定义角色、回答原则和注入 RAG 上下文。
     * </p>
     */
    private String buildSystemPrompt(String ragContext) {
        return """
                你是一个智能助手，拥有以下能力：
                1. 基于知识库文档回答问题（RAG检索增强）
                2. 调用工具获取实时信息（当前时间、订单数据、风控图谱关系）

                ## 回答原则
                - 优先使用知识库中的资料回答问题
                - 涉及实时数据（时间、订单、实体关联关系）时调用对应工具获取
                - 回答使用 Markdown 格式，结构清晰
                - 如果知识库中没有相关信息，可以结合你的知识和工具来回答

                ## 知识库资料
                """ + ragContext;
    }

    /**
     * Agent 执行结果
     *
     * @param conversationId 会话ID
     * @param answer         最终答案
     * @param tokenUsed      本次消耗的 token 数
     */
    public record AgentResult(Long conversationId, String answer, int tokenUsed) {
    }
}
