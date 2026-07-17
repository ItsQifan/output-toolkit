package com.zhouchuanxiang.outputtoolkit.agentrag.rag;

import com.zhouchuanxiang.outputtoolkit.agentrag.entity.DocumentChunk;
import com.zhouchuanxiang.outputtoolkit.agentrag.util.MarkdownSplitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 文本向量化服务
 * <p>
 * 负责将文本块切分并准备好用于向量化的数据。
 * 实际的向量化由 DocumentService 调用 EmbeddingModel 完成，
 * 此服务主要负责文本切分和块元数据管理。
 * </p>
 *
 * @author qifan
 * @since 2026-07-13
 */
//tips: "Embedding（向量化）"这个词听起来很高大上，但原理其实很简单。
//     想象你要给100本书分类。你给每本书打上标签：科幻/历史/技术/小说...
//     但一个人只能打几个标签，而且"这本书50%像科幻、30%像冒险"怎么表示？
//     向量化就是给每段文字生成几百个数字（比如1536个），
//     每个数字代表它在某个维度上的"特征强度"。两段文字语义越接近，
//     它们的向量在高维空间中离得越近——这就是 RAG 检索的数学基础。
@Slf4j
@Service
public class EmbeddingService {

    private final MarkdownSplitter splitter;

    public EmbeddingService() {
        this.splitter = new MarkdownSplitter();
    }

    /**
     * 将文本切分为块并创建元数据记录
     * <p>
     * 执行步骤：
     * <ol>
     *   <li>使用 MarkdownSplitter 将文本切分为块</li>
     *   <li>为每个块生成唯一 chunkId</li>
     *   <li>创建 DocumentChunk 记录（不含向量，向量由 DocumentService 后续填充）</li>
     * </ol>
     * </p>
     *
     * @param text         原始文本
     * @param documentName 文档名称
     * @return 切分结果（块文本列表 + 块元数据）
     */
    public EmbeddingResult embedAndSplit(String text, String documentName) {
        // 第一步：文本切分
        List<String> chunks = splitter.split(text);
        log.info("向量化_文本切分完成, documentName={}, chunkCount={}", documentName, chunks.size());

        // 第二步：创建块元数据
        List<DocumentChunk> chunkRecords = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            String chunkContent = chunks.get(i);
            String chunkId = UUID.randomUUID().toString();

            DocumentChunk chunk = DocumentChunk.builder()
                    .id(chunkId)
                    .documentName(documentName)
                    .chunkIndex(i)
                    .content(chunkContent)
                    .createdAt(LocalDateTime.now())
                    .build();
            chunkRecords.add(chunk);
        }

        return new EmbeddingResult(chunks, chunkRecords);
    }

    /**
     * 嵌入结果 —— 包含切分后的文本列表和块元数据
     */
    public record EmbeddingResult(List<String> chunks, List<DocumentChunk> chunkRecords) {
    }
}
