package com.zhouchuanxiang.outputtoolkit.agentrag.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文档块实体
 * <p>
 * 记录上传文档的分块信息和对应的向量索引，用于向量检索后关联回原始文档。
 * 注意：此实体目前仅用于内存管理，向量数据存储在 Milvus 中。
 * </p>
 *
 * @author qifan
 * @since 2026-07-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentChunk {

    /** 块ID */
    private String id;

    /** 所属文档名 */
    private String documentName;

    /** 块序号（从0开始） */
    private Integer chunkIndex;

    /** 块文本内容 */
    private String content;

    /** 向量存储中的ID（Milvus 生成的唯一标识） */
    private String vectorId;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
