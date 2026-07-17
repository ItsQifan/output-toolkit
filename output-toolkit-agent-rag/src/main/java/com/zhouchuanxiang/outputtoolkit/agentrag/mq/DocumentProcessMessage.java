package com.zhouchuanxiang.outputtoolkit.agentrag.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文档处理消息（Kafka 消息体）
 * <p>
 * 上传接口发送、消费者接收的消息契约，以 JSON 字符串形式在 Kafka 中传输。
 * 只携带文档ID和文件路径等轻量信息，文档内容由消费者从磁盘读取，
 * 避免大文件内容直接塞进 Kafka 消息（Kafka 默认单条消息上限 1MB）。
 * </p>
 *
 * @author qifan
 * @since 2026-07-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentProcessMessage {

    /** 文档ID（t_document 主键，消费者据此更新处理状态） */
    private Long documentId;

    /** 文档文件名（写入 Milvus 元数据，供检索溯源与按文档删除） */
    private String filename;

    /** 文件存储路径（消费者从此路径读取文档内容） */
    private String filePath;
}
