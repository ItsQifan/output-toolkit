package com.zhouchuanxiang.outputtoolkit.agentrag.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 知识文档实体（对应 t_document 表）
 * <p>
 * 记录上传文档的处理状态，支撑 Kafka 异步文档处理流水线的状态追踪：
 * 上传接口插入 PENDING → 消费者开始处理更新 PROCESSING → 完成 COMPLETED / 失败 FAILED。
 * </p>
 *
 * @author qifan
 * @since 2026-07-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeDocument {

    /** 处理状态：待处理（消息已发送，等待消费者处理） */
    public static final String STATUS_PENDING = "PENDING";

    /** 处理状态：处理中（消费者已开始切分/向量化） */
    public static final String STATUS_PROCESSING = "PROCESSING";

    /** 处理状态：处理完成（向量已写入 Milvus） */
    public static final String STATUS_COMPLETED = "COMPLETED";

    /** 处理状态：处理失败（error_msg 记录失败原因） */
    public static final String STATUS_FAILED = "FAILED";

    /** 文档ID */
    private Long id;

    /** 文档文件名 */
    private String filename;

    /** 文件在服务器上的存储路径 */
    private String filePath;

    /** 处理状态：PENDING/PROCESSING/COMPLETED/FAILED */
    private String status;

    /** 文档分块数量（处理完成后填充） */
    private Integer chunkCount;

    /** 处理失败的异常信息 */
    private String errorMsg;

    /** 上传时间 */
    private LocalDateTime createdAt;

    /** 最后更新时间 */
    private LocalDateTime updatedAt;
}
