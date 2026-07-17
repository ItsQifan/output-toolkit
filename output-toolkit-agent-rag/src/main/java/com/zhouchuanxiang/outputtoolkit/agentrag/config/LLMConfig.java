package com.zhouchuanxiang.outputtoolkit.agentrag.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LLM 配置
 * <p>
 * Spring AI 通过 application.yml 中的 spring.ai.openai.* 配置自动创建
 * ChatModel 和 EmbeddingModel Bean；此处基于 ChatModel 构建 ChatClient，
 * 作为原生 Tool Calling 的统一调用入口。
 * </p>
 *
 * @author qifan
 * @since 2026-07-13
 */
//tips: ChatModel 和 ChatClient 的关系类似 JDBC 的 Connection 和 JdbcTemplate：
//     ChatModel 是底层模型接口（一次 call 一次请求），
//     ChatClient 是高层流式 API（fluent 链式调用），
//     支持 .system() .user() .tools() .toolContext() 等能力组合，
//     其中 .tools() 会让框架自动完成"LLM请求工具→执行→结果回传→继续推理"的多轮循环。
@Configuration
public class LLMConfig {

    /**
     * 构建 ChatClient（原生 Tool Calling 的调用入口）
     *
     * @param chatModel Spring AI 自动配置的聊天模型（OpenAI 兼容协议）
     * @return ChatClient 实例
     */
    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }
}
