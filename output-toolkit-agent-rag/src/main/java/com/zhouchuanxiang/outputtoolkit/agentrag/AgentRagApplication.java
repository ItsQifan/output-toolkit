package com.zhouchuanxiang.outputtoolkit.agentrag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Agent RAG Demo —— 主启动类
 * <p>
 * 本项目是一个单 Agent + RAG（检索增强生成）+ 工具调用（Function Calling）的演示模块，
 * 用于个人展示展示 Agent 基础原理。核心流程：
 * <ol>
 *   <li><b>文档上传</b>：上传 Markdown/TXT 文件 → 文本分段 → 向量化 → 存入 Milvus 向量库</li>
 *   <li><b>用户提问</b>：通过聊天界面或 API 提交问题</li>
 *   <li><b>向量检索</b>：将问题向量化 → 在 Milvus 中检索最相关的文档片段（Top-K）</li>
 *   <li><b>Agent 编排</b>：ReAct 循环（Thought → Action → Observation）—— LLM 决定是否调用工具</li>
 *   <li><b>工具调用</b>：内置时间查询工具 + 数据库订单查询工具，LLM 自主决定何时调用</li>
 *   <li><b>流式回答</b>：通过 SSE 实时推送回答内容 + 推理链路可视化</li>
 *   <li><b>会话持久化</b>：对话历史存入 MySQL，支持多轮对话上下文管理</li>
 * </ol>
 * </p>
 *
 * @author qifan
 * @since 2026-07-13
 */
@SpringBootApplication(scanBasePackages = "com.zhouchuanxiang.outputtoolkit.agentrag")
public class AgentRagApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentRagApplication.class, args);
    }
}
