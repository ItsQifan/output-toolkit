# Agent RAG —— 知识问答平台核心链路

> 单 Agent + RAG + 原生 Tool Calling 的企业知识问答实现，覆盖「文档异步入库 → 语义检索 → 增强生成 → 动态工具调用 → 缓存加速」完整链路。

## 项目概述

这是一个基于 Spring Boot 3.5.7 + Spring AI 1.1.7 的知识问答平台核心模块，展示了以下核心技术：

| 技术点 | 说明 |
|--------|------|
| **RAG（检索增强生成）** | 文档清洗切分 → Embedding 向量化 → Milvus 语义检索 → 增强 LLM 回答 |
| **Milvus 向量数据库** | IVF_FLAT 索引 + 余弦相似度，ANN 近似最近邻检索，数据持久化 |
| **Kafka 异步入库** | 文档上传与向量化解耦：上传毫秒级受理，切分/Embedding/索引构建异步完成 |
| **原生 Tool Calling** | @Tool 注解 + ChatClient，3 个工具——时间查询 / 订单查询 / 风控图谱关系查询 |
| **Redis 两级缓存** | L1 热点问答缓存 + L2 检索结果缓存，减少重复计算与模型调用 |
| **SSE 流式响应** | 实时推送 Agent 推理链路（检索 → 工具调用 → 回答） |
| **会话持久化** | MySQL 存储完整对话历史 + 上下文窗口管理 |

## 架构图

```
浏览器 (Chat UI)
    │ SSE (流式)
    ▼
ChatController ──→ AgentService (ChatClient 原生 Tool Calling)
                      │
        ┌──────┬──────┼──────────┬───────────┐
        ▼      ▼      ▼          ▼           ▼
    L1问答缓存  RAG检索  时间工具  订单查询工具  图谱查询工具
        │      │(L2缓存)                │          │
        ▼      ▼                       └────┬─────┘
      Redis  Milvus                        MySQL
             (IVF_FLAT+COSINE)      (会话+订单+图谱数据)

文档入库链路（异步）：
DocumentController ──→ 文件落地 + t_document(PENDING) + 发送消息
                            │
                            ▼
                     Kafka (doc-process topic)
                            │
                            ▼
              DocumentProcessConsumer（手动ack，至少一次语义）
                清洗切分 → Embedding → 写入 Milvus → COMPLETED/FAILED
```

## 快速启动

### 1. 环境准备

```bash
cd output-toolkit-agent-rag

# 一键启动 Milvus 2.4 + Kafka 3.7(KRaft) + Redis 6.2
docker-compose up -d

# MySQL 使用本地实例（或取消 docker-compose.yml 中 mysql 服务的注释）
# 首次需执行初始化脚本：schema.sql + data.sql（或增量脚本 docker/migration-upgrade.sql）
```

### 2. 配置 LLM

```bash
# 设置环境变量（OpenAI 兼容协议）
export OPENAI_API_KEY=sk-your-key
export OPENAI_BASE_URL=https://api.deepseek.com
export LLM_MODEL=deepseek-chat
export EMBEDDING_MODEL=text-embedding-ada-002
```

> 支持任意 OpenAI 兼容 API：DeepSeek、Qwen、智谱GLM、Ollama 等。
> 注意：Embedding 需要供应商支持 embedding 接口，且向量维度需与
> `spring.ai.vectorstore.milvus.embedding-dimension`（默认 1536）一致。

### 3. 启动应用

```bash
cd output-toolkit-agent-rag
mvn spring-boot:run
```

### 4. 访问

- **聊天界面**: http://localhost:8080/
- **Swagger API**: http://localhost:8080/swagger-ui.html

## 项目结构

```
output-toolkit-agent-rag/
├── pom.xml
├── docker-compose.yml                     # Milvus + Kafka + Redis 编排
├── docker/
│   ├── embedEtcd.yaml                    # Milvus 内嵌 etcd 配置
│   └── migration-upgrade.sql             # 增量初始化脚本
├── README.md
└── src/main/java/.../agentrag/
    ├── AgentRagApplication.java          # 启动类
    ├── config/                            # 配置类
    │   ├── LLMConfig.java                # ChatClient Bean（原生 Tool Calling 入口）
    │   ├── WebMvcConfig.java             # CORS + 静态资源
    │   └── SpringDocConfig.java          # Swagger配置
    ├── controller/                        # REST控制器
    │   ├── ChatController.java           # SSE流式聊天API
    │   ├── DocumentController.java       # 文档上传（异步受理）+ 状态轮询API
    │   └── IndexController.java          # 页面路由
    ├── agent/                             # Agent核心
    │   └── AgentService.java             # ★ ChatClient 编排 + 两级缓存集成（核心！）
    ├── rag/                               # RAG检索
    │   ├── DocumentService.java          # ★ 文档处理流水线（上传受理+异步处理两阶段）
    │   ├── DocumentMapper.java           # t_document 状态DAO
    │   ├── EmbeddingService.java         # 文本切分服务
    │   └── RetrievalService.java         # ★ Milvus 向量检索（VectorStore）
    ├── mq/                                # Kafka 异步链路
    │   ├── DocumentProcessMessage.java   # 消息契约
    │   ├── DocumentProcessProducer.java  # 生产者（上传接口调用）
    │   └── DocumentProcessConsumer.java  # 消费者（手动ack+幂等处理）
    ├── cache/                             # Redis 缓存
    │   └── CacheService.java             # ★ 两级缓存（问题归一化+SCAN渐进清理）
    ├── tool/                              # 内置工具（@Tool 注解）
    │   ├── DateTimeTool.java             # 时间查询工具（含 ToolEventSupport）
    │   ├── OrderQueryTool.java           # 订单查询工具
    │   └── GraphQueryTool.java           # ★ 风控图谱关系查询（1~2度BFS展开）
    ├── session/                           # 会话管理
    │   ├── SessionService.java           # 会话CRUD + 消息持久化
    │   ├── ConversationMapper.java       # 会话DAO
    │   ├── MessageMapper.java            # 消息DAO
    │   └── ContextWindowManager.java     # 上下文窗口管理
    ├── entity/                            # 数据实体
    ├── dto/                               # 数据传输对象
    ├── exception/                         # 异常处理
    └── util/                              # 工具类
        ├── MarkdownSplitter.java         # Markdown智能分块
        └── RetryUtil.java                # 指数退避重试
```

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/` | 聊天页面 |
| `GET` | `/api/chat/stream` | SSE 流式聊天 |
| `GET` | `/api/conversations` | 会话列表 |
| `GET` | `/api/conversations/{id}/messages` | 历史消息 |
| `DELETE` | `/api/conversations/{id}` | 删除会话 |
| `POST` | `/api/document/upload` | 上传文档（异步受理，返回 documentId） |
| `GET` | `/api/document/{id}/status` | 查询文档处理状态（PENDING/PROCESSING/COMPLETED/FAILED） |
| `GET` | `/api/document/list` | 文档列表（含处理状态） |
| `DELETE` | `/api/document/{filename}` | 删除文档 |

## 演示脚本

```
[打开 http://localhost:8080]
1. 文档上传：拖入一份 Markdown 知识手册 → 立即受理，状态自动轮询到"处理完成"
   （背后：Kafka 消息 → 消费者切分/向量化 → 写入 Milvus）
2. RAG问答：提问"账户被冻结了怎么解冻" → 展示 Milvus 语义检索 + 增强生成
3. 工具调用：提问"用户U1003有什么关联风险" → Agent 调用图谱工具（2度展开团伙线索）
4. 缓存加速：新会话再问同一问题 → L1 缓存命中，毫秒级返回
5. 会话管理：新建/切换/删除会话，历史存在MySQL

核心技术亮点：
- Milvus 向量检索（IVF_FLAT 索引 + 余弦相似度 + 元数据过滤删除）
- Kafka 异步文档流水线（手动ack + 至少一次语义 + 幂等先删后写）
- Spring AI 原生 Tool Calling（@Tool 注解，框架自动完成工具执行循环）
- Redis 两级缓存（问题归一化MD5 + SCAN渐进清理 + 故障自动降级）
- SSE流式响应 + 推理链可视化（ToolContext 传递事件消费者）
```

## 技术要点

- **Agent 核心**：`AgentService.executeStream()` 通过 ChatClient `.tools()` 注册工具，框架自动完成"LLM 决策 → 执行工具 → 结果回传 → 继续推理"的多轮循环
- **RAG 检索**：`RetrievalService` 基于 Spring AI `VectorStore` 抽象对接 Milvus，`SearchRequest` 支持 topK + 相似度阈值
- **异步入库**：上传接口只做"存文件 + 记状态 + 发消息"三件事毫秒级返回；消费者手动提交 offset 保证至少一次处理，先删旧向量再写实现幂等
- **两级缓存**：L1 答案缓存仅新会话首问生效（避免多轮上下文污染）；L2 检索缓存减少 Embedding API 调用；知识库变更全量失效 + TTL 兜底
- **图谱工具**：`GraphQueryTool` 以实体为起点 BFS 展开 1~2 度关系，模拟风控"设备聚集/IP聚集/交易聚集"的团伙挖掘场景
- **会话管理**：JdbcTemplate + MySQL，支持多轮对话和上下文窗口滑动
- **文本分块**：`MarkdownSplitter` 支持按标题层级+段落+句子多级切分
- **重试机制**：`RetryUtil` 指数退避重试（3次，1s→2s→4s）+ Spring AI 内置 retry 配置

## 代码注释

项目中所有核心类和方法都包含 `//tips:` 注释，用通俗易懂的自然语言解释了 Agent、RAG、Embedding、Tool Calling、Kafka 消费语义、缓存设计等概念，适合学习新技术时参考。
