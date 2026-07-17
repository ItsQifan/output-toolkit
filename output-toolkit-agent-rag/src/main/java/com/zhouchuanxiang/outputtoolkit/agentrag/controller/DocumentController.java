package com.zhouchuanxiang.outputtoolkit.agentrag.controller;

import com.zhouchuanxiang.outputtoolkit.agentrag.entity.KnowledgeDocument;
import com.zhouchuanxiang.outputtoolkit.agentrag.rag.DocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 文档管理控制器
 * <p>
 * 提供文档上传、状态查询、列表、删除等 REST API。
 * 文档上传采用异步受理模式：接口只负责文件落地 + 发送 Kafka 消息，
 * 切分/向量化/入库由消费者后台完成，前端通过状态接口轮询处理进度。
 * 日志前缀：文档管理_
 * </p>
 *
 * @author qifan
 * @since 2026-07-13
 */
@Slf4j
@RestController
@RequestMapping("/api/document")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * 上传文档（异步受理）
     * <p>
     * 接收 Markdown/TXT 文件，文件落地后发送 Kafka 消息立即返回（毫秒级），
     * 后台消费者异步执行：清洗 → 切分 → Embedding 向量化 → 写入 Milvus。
     * 返回的 documentId 可用于轮询处理状态。
     * </p>
     *
     * @param file 上传的文件（multipart/form-data）
     * @return 受理结果（含 documentId 和初始状态 PENDING）
     */
    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Map.of("code", 400, "msg", "文件为空");
        }

        String filename = file.getOriginalFilename();
        log.info("文档管理_收到上传请求, filename={}, size={}", filename, file.getSize());

        try {
            DocumentService.DocumentUploadResult result = documentService.uploadDocument(file);
            log.info("文档管理_上传受理成功, documentId={}, filename={}", result.documentId(), result.filename());
            return Map.of(
                    "code", 200,
                    "msg", "上传成功，文档正在后台处理",
                    "data", Map.of(
                            "documentId", result.documentId(),
                            "filename", result.filename(),
                            "status", result.status()
                    )
            );
        } catch (IOException e) {
            log.error("文档管理_文件保存失败, filename={}", filename, e);
            return Map.of("code", 500, "msg", "文件保存失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("文档管理_上传受理失败, filename={}", filename, e);
            return Map.of("code", 500, "msg", "上传受理失败：" + e.getMessage());
        }
    }

    /**
     * 查询文档处理状态（供前端轮询异步处理进度）
     *
     * @param id 文档ID（上传接口返回的 documentId）
     * @return 文档处理状态详情
     */
    @GetMapping("/{id}/status")
    public Map<String, Object> status(@PathVariable Long id) {
        KnowledgeDocument document = documentService.getDocumentStatus(id);
        if (document == null) {
            return Map.of("code", 404, "msg", "文档不存在");
        }
        Map<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("documentId", document.getId());
        data.put("filename", document.getFilename());
        data.put("status", document.getStatus());
        data.put("chunkCount", document.getChunkCount());
        // 失败时返回错误原因，便于前端提示
        data.put("errorMsg", document.getErrorMsg() != null ? document.getErrorMsg() : "");
        return Map.of("code", 200, "data", data);
    }

    /**
     * 获取已上传文档列表（含处理状态）
     *
     * @return 文档列表
     */
    @GetMapping("/list")
    public Map<String, Object> list() {
        List<DocumentService.DocumentUploadResult> documents = documentService.listDocuments();
        return Map.of("code", 200, "data", documents);
    }

    /**
     * 删除文档
     *
     * @param filename 文档文件名
     * @return 操作结果
     */
    @DeleteMapping("/{filename}")
    public Map<String, Object> delete(@PathVariable String filename) {
        log.info("文档管理_删除请求, filename={}", filename);
        boolean deleted = documentService.deleteDocument(filename);
        if (deleted) {
            return Map.of("code", 200, "msg", "删除成功");
        } else {
            return Map.of("code", 404, "msg", "文档不存在");
        }
    }
}
