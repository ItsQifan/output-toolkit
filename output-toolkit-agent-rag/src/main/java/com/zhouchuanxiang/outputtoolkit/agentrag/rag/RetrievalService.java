package com.zhouchuanxiang.outputtoolkit.agentrag.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 向量检索服务（基于 Milvus 向量数据库）
 * <p>
 * 负责将文档块写入 Milvus，以及将用户问题向量化后在 Milvus 中做语义检索。
 * 底层通过 Spring AI 的 {@link VectorStore} 抽象对接 MilvusVectorStore，
 * Embedding 计算由 VectorStore 内部自动调用 EmbeddingModel 完成。
 * </p>
 * <p>
 * 使用原因：检索是 RAG 区别于普通 LLM 对话的关键——普通 LLM 只能凭训练记忆回答，
 * RAG 可以先"翻书"（检索相关文档），再结合检索结果回答，减少幻觉。
 * 早期版本使用 ConcurrentHashMap 内存存储 + 自实现余弦相似度，
 * 已替换为 Milvus：支持 ANN 索引（IVF_FLAT）、数据持久化、海量向量毫秒级检索。
 * </p>
 * <p>
 * 完整类结构：
 * <ul>
 *   <li>RetrievalService —— 检索服务，封装向量写入/检索/删除逻辑</li>
 *   <li>VectorStore（MilvusVectorStore）—— Spring AI 向量库抽象，自动完成 Embedding + 存取</li>
 *   <li>RetrievalResult —— 检索结果记录（文本 + 元数据 + 相似度评分）</li>
 * </ul>
 * </p>
 *
 * @author qifan
 * @since 2026-07-13
 */
//tips: "检索增强生成（RAG）"这个名字拆开看就很好理解：
//     R=Retrieval（检索）：用户提问时，先去知识库里搜相关的资料片段。
//     A=Augmented（增强）：把搜到的资料和用户问题拼接在一起，作为 LLM 的输入。
//     G=Generation（生成）：LLM 阅读拼接后的内容，生成答案。
//     整个过程就像开卷考试——你先翻书找到相关知识，再结合知识回答问题，
//     而不是仅凭记忆力（模型训练时的知识）硬猜。
//tips: 为什么换成 Milvus？内存版 HashMap 检索是 O(n) 暴力遍历，
//     十万级向量就会明显变慢，且应用重启后数据全丢。
//     Milvus 通过 IVF_FLAT 索引（先聚类分桶，检索时只扫最近的几个桶）
//     把复杂度降到近似 O(log n)，并且数据落盘持久化。
@Slf4j
@Service
public class RetrievalService {

    /** Milvus 元数据字段：文档名（写入与删除时的过滤键） */
    public static final String META_DOCUMENT_NAME = "documentName";

    /** Milvus 元数据字段：块序号 */
    public static final String META_CHUNK_INDEX = "chunkIndex";

    private final VectorStore vectorStore;

    public RetrievalService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * 批量写入文档块到 Milvus
     * <p>
     * VectorStore.add() 内部会自动调用 EmbeddingModel 将文本批量向量化，
     * 再连同元数据一起写入 Milvus collection。
     * </p>
     *
     * @param documents Spring AI 文档列表（含文本内容和元数据）
     */
    public void addDocuments(List<Document> documents) {
        vectorStore.add(documents);
        log.info("向量存储_批量写入完成, count={}", documents.size());
    }

    /**
     * 语义检索与查询最相关的文档片段
     * <p>
     * 执行步骤：
     * <ol>
     *   <li>查询文本向量化（VectorStore 内部调用 EmbeddingModel）</li>
     *   <li>Milvus 基于 IVF_FLAT 索引 + 余弦相似度执行 ANN 检索</li>
     *   <li>按相似度阈值过滤，返回 Top-K 结果</li>
     * </ol>
     * </p>
     *
     * @param query     用户查询文本
     * @param topK      返回 Top-K 个最相似的片段
     * @param threshold 最低相似度阈值（0~1）
     * @return 检索到的文档片段列表
     */
    public List<RetrievalResult> retrieve(String query, int topK, double threshold) {
        log.info("RAG检索_开始检索, query={}, topK={}, threshold={}",
                query.length() > 50 ? query.substring(0, 50) + "..." : query, topK, threshold);

        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(threshold)
                .build();
        List<Document> documents = vectorStore.similaritySearch(request);

        // Document → RetrievalResult 转换，屏蔽 Spring AI 类型对上层的渗透
        List<RetrievalResult> results = documents.stream()
                .map(doc -> new RetrievalResult(
                        doc.getId(),
                        doc.getText(),
                        doc.getMetadata(),
                        doc.getScore() != null ? doc.getScore() : 0.0))
                .toList();

        log.info("RAG检索_检索完成, matchedResults={}", results.size());
        return results;
    }

    /**
     * 删除指定文档的所有向量（通过元数据 documentName 过滤删除）
     * <p>
     * Milvus 支持按标量字段过滤删除，无需逐条记录向量 ID。
     * </p>
     *
     * @param documentName 文档名
     */
    public void removeByDocument(String documentName) {
        FilterExpressionBuilder builder = new FilterExpressionBuilder();
        vectorStore.delete(builder.eq(META_DOCUMENT_NAME, documentName).build());
        log.info("向量存储_删除文档向量, documentName={}", documentName);
    }

    /**
     * 检索结果
     *
     * @param documentId      向量库中的文档块 ID
     * @param content         块文本内容
     * @param metadata        元数据（documentName、chunkIndex 等）
     * @param similarityScore 相似度评分（0~1，越大越相似）
     */
    public record RetrievalResult(
            String documentId,
            String content,
            Map<String, Object> metadata,
            double similarityScore
    ) {
    }
}
