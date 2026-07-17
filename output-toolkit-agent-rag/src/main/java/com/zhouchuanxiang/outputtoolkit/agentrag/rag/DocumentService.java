package com.zhouchuanxiang.outputtoolkit.agentrag.rag;

import com.zhouchuanxiang.outputtoolkit.agentrag.cache.CacheService;
import com.zhouchuanxiang.outputtoolkit.agentrag.entity.DocumentChunk;
import com.zhouchuanxiang.outputtoolkit.agentrag.entity.KnowledgeDocument;
import com.zhouchuanxiang.outputtoolkit.agentrag.mq.DocumentProcessMessage;
import com.zhouchuanxiang.outputtoolkit.agentrag.mq.DocumentProcessProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 文档处理服务（Facade 模式 —— 文档处理流水线门面）
 * <p>
 * 负责文档处理的两个阶段：
 * <ol>
 *   <li>上传阶段（同步，毫秒级）：文件落地 → 写入 t_document(PENDING) → 发送 Kafka 消息 → 立即返回</li>
 *   <li>处理阶段（异步，Kafka 消费者触发）：读取文件 → 清洗切分 → Embedding 向量化 → 写入 Milvus → 更新状态</li>
 * </ol>
 * </p>
 * <p>
 * ① 使用原因：Embedding 向量化耗时（逐块调用远程 API），同步处理会阻塞上传接口；
 * 通过 Kafka 解耦"接收"与"处理"，上传接口不再受文档大小影响。
 * ② 模式收益：Facade 对外仅暴露 upload/process/list/delete 四个入口，
 * 内部协调 EmbeddingService、RetrievalService、DocumentMapper、Kafka 生产者的协作。
 * ③ 完整类结构：
 * <ul>
 *   <li>DocumentService —— 文档处理门面</li>
 *   <li>EmbeddingService —— 文本清洗与切分</li>
 *   <li>RetrievalService —— Milvus 向量写入/删除</li>
 *   <li>DocumentMapper —— t_document 状态持久化</li>
 *   <li>DocumentProcessProducer/Consumer —— Kafka 异步消息链路</li>
 * </ul>
 * </p>
 *
 * @author qifan
 * @since 2026-07-13
 */
//tips: 改造前的流程是"上传接口里同步做完所有事"，用户传一个大文档要等几十秒。
//     改造后上传接口只做三件事（存文件、记状态、发消息），毫秒级返回；
//     真正耗时的向量化在消费者线程里慢慢做，前端通过状态接口轮询进度。
//     这就是"异步化"的核心思想：把"必须马上做"和"可以稍后做"的事拆开。
@Slf4j
@Service
public class DocumentService {

    private final EmbeddingService embeddingService;
    private final RetrievalService retrievalService;
    private final DocumentMapper documentMapper;
    private final DocumentProcessProducer documentProcessProducer;
    private final CacheService cacheService;

    /** 上传文件的本地存储目录 */
    private final String uploadDir;

    public DocumentService(EmbeddingService embeddingService,
                           RetrievalService retrievalService,
                           DocumentMapper documentMapper,
                           DocumentProcessProducer documentProcessProducer,
                           CacheService cacheService,
                           @Value("${agent-rag.upload-dir:./data/uploads}") String uploadDir) {
        this.embeddingService = embeddingService;
        this.retrievalService = retrievalService;
        this.documentMapper = documentMapper;
        this.documentProcessProducer = documentProcessProducer;
        this.cacheService = cacheService;
        this.uploadDir = uploadDir;
    }

    /**
     * 上传文档（异步入库的第一阶段，快速返回）
     * <p>
     * 执行步骤：
     * <ol>
     *   <li>文件落地到本地存储目录（文档ID前缀避免同名覆盖）</li>
     *   <li>插入 t_document 记录，状态 PENDING</li>
     *   <li>发送 Kafka 处理消息，由消费者异步完成切分/向量化/入库</li>
     * </ol>
     * </p>
     *
     * @param file 上传的文件
     * @return 上传受理结果（含文档ID，供前端轮询处理状态）
     * @throws IOException 文件保存失败时抛出
     */
    public DocumentUploadResult uploadDocument(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown.md";
        log.info("文档上传_接收文件, filename={}, size={} bytes", filename, file.getSize());

        // 第一步：先插入状态记录拿到文档ID（ID 用于文件名前缀，保证同名文档不互相覆盖）
        KnowledgeDocument document = KnowledgeDocument.builder()
                .filename(filename)
                .filePath("")
                .status(KnowledgeDocument.STATUS_PENDING)
                .build();
        documentMapper.insert(document);

        // 第二步：文件落地
        Path dir = Paths.get(uploadDir);
        Files.createDirectories(dir);
        Path filePath = dir.resolve(document.getId() + "_" + filename);
        file.transferTo(filePath.toAbsolutePath().toFile());

        // 回填真实存储路径
        updateFilePath(document.getId(), filePath.toAbsolutePath().toString());

        // 第三步：发送 Kafka 消息，触发异步处理
        documentProcessProducer.sendDocProcessMessage(DocumentProcessMessage.builder()
                .documentId(document.getId())
                .filename(filename)
                .filePath(filePath.toAbsolutePath().toString())
                .build());

        log.info("文档上传_受理成功已发送异步处理消息, documentId={}, filename={}", document.getId(), filename);
        return new DocumentUploadResult(document.getId(), filename, KnowledgeDocument.STATUS_PENDING, 0);
    }

    /**
     * 处理文档（异步入库的第二阶段，由 Kafka 消费者调用）
     * <p>
     * 执行步骤：
     * <ol>
     *   <li>状态更新为 PROCESSING</li>
     *   <li>读取文件内容并清洗切分（MarkdownSplitter）</li>
     *   <li>幂等保障：先删除该文档的旧向量（应对消息重复消费）</li>
     *   <li>批量写入 Milvus（VectorStore 内部自动完成 Embedding 计算）</li>
     *   <li>状态更新为 COMPLETED，清空问答缓存（知识库已变更）</li>
     * </ol>
     * 业务异常统一捕获并标记 FAILED，异常信息落库供排查。
     * </p>
     *
     * @param documentId 文档ID
     * @param filename   文档文件名
     * @param filePath   文件存储路径
     */
    public void processDocument(Long documentId, String filename, String filePath) {
        log.info("文档处理_开始异步处理, documentId={}, filename={}", documentId, filename);
        documentMapper.updateStatus(documentId, KnowledgeDocument.STATUS_PROCESSING);

        try {
            // 第一步：读取文件内容（UTF-8）
            String content = Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);

            // 第二步：清洗切分
            EmbeddingService.EmbeddingResult result = embeddingService.embedAndSplit(content, filename);
            List<DocumentChunk> chunks = result.chunkRecords();

            // 第三步：幂等保障——先清理该文档可能存在的旧向量（消息重复消费或文档重新上传场景）
            retrievalService.removeByDocument(filename);

            // 第四步：组装 Spring AI Document 并批量写入 Milvus
            List<Document> documents = new ArrayList<>(chunks.size());
            for (DocumentChunk chunk : chunks) {
                documents.add(new Document(chunk.getId(), chunk.getContent(), Map.of(
                        RetrievalService.META_DOCUMENT_NAME, filename,
                        RetrievalService.META_CHUNK_INDEX, chunk.getChunkIndex())));
            }
            retrievalService.addDocuments(documents);

            // 第五步：标记完成 + 清空问答缓存（知识库内容已变更，旧缓存可能过期）
            documentMapper.markCompleted(documentId, chunks.size());
            cacheService.evictAll();
            log.info("文档处理_处理完成, documentId={}, filename={}, chunkCount={}", documentId, filename, chunks.size());
        } catch (IOException e) {
            log.error("文档处理_文件读取失败, documentId={}, filePath={}", documentId, filePath, e);
            documentMapper.markFailed(documentId, "文件读取失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("文档处理_向量化入库失败, documentId={}, filename={}", documentId, filename, e);
            documentMapper.markFailed(documentId, "向量化入库失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有文档及其处理状态
     *
     * @return 文档信息列表
     */
    public List<DocumentUploadResult> listDocuments() {
        return documentMapper.selectAll().stream()
                .map(doc -> new DocumentUploadResult(doc.getId(), doc.getFilename(), doc.getStatus(),
                        doc.getChunkCount() != null ? doc.getChunkCount() : 0))
                .toList();
    }

    /**
     * 查询单个文档的处理状态
     *
     * @param documentId 文档ID
     * @return 文档状态，不存在返回 null
     */
    public KnowledgeDocument getDocumentStatus(Long documentId) {
        return documentMapper.selectById(documentId);
    }

    /**
     * 删除文档及其向量数据
     * <p>
     * 依次清理：Milvus 向量 → t_document 记录 → 本地文件 → 问答缓存。
     * </p>
     *
     * @param documentName 文档名称
     * @return 是否删除成功
     */
    public boolean deleteDocument(String documentName) {
        KnowledgeDocument document = documentMapper.selectByFilename(documentName);
        if (document == null) {
            log.warn("文档删除_文档不存在, documentName={}", documentName);
            return false;
        }

        // 清理 Milvus 向量
        retrievalService.removeByDocument(documentName);
        // 清理状态记录
        documentMapper.deleteById(document.getId());
        // 清理本地文件（失败不影响主流程，只记录日志）
        try {
            if (document.getFilePath() != null && !document.getFilePath().isBlank()) {
                Files.deleteIfExists(Paths.get(document.getFilePath()));
            }
        } catch (IOException e) {
            log.warn("文档删除_本地文件删除失败, filePath={}, error={}", document.getFilePath(), e.getMessage());
        }
        // 知识库已变更，清空问答缓存
        cacheService.evictAll();

        log.info("文档删除_删除完成, documentName={}, documentId={}", documentName, document.getId());
        return true;
    }

    /**
     * 更新文档的存储路径
     *
     * @param documentId 文档ID
     * @param filePath   文件存储路径
     */
    private void updateFilePath(Long documentId, String filePath) {
        // 简单直接的路径回填，复用 DocumentMapper 的 JdbcTemplate 会引入额外方法，
        // 此处通过专用 SQL 更新（保持 DAO 职责单一）
        documentMapper.updateFilePath(documentId, filePath);
    }

    /**
     * 文档上传/状态查询结果
     *
     * @param documentId 文档ID（用于状态轮询）
     * @param filename   文档文件名
     * @param status     处理状态: PENDING/PROCESSING/COMPLETED/FAILED
     * @param chunkCount 分块数量（处理完成后有值）
     */
    public record DocumentUploadResult(Long documentId, String filename, String status, int chunkCount) {
    }
}
