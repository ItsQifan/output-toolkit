package com.zhouchuanxiang.outputtoolkit.agentrag.mq;

import com.alibaba.fastjson2.JSON;
import com.zhouchuanxiang.outputtoolkit.agentrag.rag.DocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * 文档处理消息消费者
 * <p>
 * 监听文档处理主题，异步完成文档处理流水线：
 * 读取文件 → 清洗切分 → Embedding 向量化 → 写入 Milvus → 更新 t_document 状态。
 * </p>
 * <p>
 * 可靠性设计：
 * <ul>
 *   <li>手动提交 offset（ack-mode=manual_immediate）：处理成功才提交，
 *       消费中途宕机时消息会被重新投递，保证"至少一次"处理语义</li>
 *   <li>业务异常（如文件不存在）标记 FAILED 后仍提交 offset，避免坏消息无限重试阻塞分区</li>
 *   <li>状态幂等：重复消费时 Milvus 先删后写（按文档名清理旧向量），结果一致</li>
 * </ul>
 * </p>
 *
 * @author qifan
 * @since 2026-07-16
 */
//tips: "至少一次（at-least-once）"和"最多一次（at-most-once）"是消息消费的两种语义：
//     先处理后提交 offset = 至少一次（宕机时消息重发，可能重复处理，需要幂等设计）；
//     先提交后处理 = 最多一次（宕机时消息丢失，但绝不重复）。
//     知识入库场景宁可重复也不能丢，所以选"至少一次"+幂等写入（先删旧向量再写新向量）。
@Slf4j
@Component
public class DocumentProcessConsumer {

    private final DocumentService documentService;

    public DocumentProcessConsumer(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * 消费文档处理消息
     * <p>
     * 执行步骤：
     * <ol>
     *   <li>反序列化消息，解析出文档ID与文件路径</li>
     *   <li>委托 DocumentService 执行切分/向量化/入库</li>
     *   <li>处理完成（无论成败，状态已落库）后手动提交 offset</li>
     * </ol>
     * </p>
     *
     * @param payload 消息体（DocumentProcessMessage 的 JSON 字符串）
     * @param ack     手动提交句柄
     */
    @KafkaListener(topics = "${agent-rag.kafka.doc-process-topic}")
    public void onDocProcessMessage(String payload, Acknowledgment ack) {
        log.info("文档管理_收到Kafka处理消息, payload={}", payload);
        try {
            DocumentProcessMessage message = JSON.parseObject(payload, DocumentProcessMessage.class);
            if (message == null || message.getDocumentId() == null) {
                // 消息格式非法，记录后直接提交 offset 丢弃，避免坏消息阻塞分区
                log.error("文档管理_消息格式非法已丢弃, payload={}", payload);
                ack.acknowledge();
                return;
            }
            // 核心处理：切分 → 向量化 → 写入 Milvus → 更新状态（内部已捕获业务异常并标记 FAILED）
            documentService.processDocument(message.getDocumentId(), message.getFilename(), message.getFilePath());
            ack.acknowledge();
        } catch (Exception e) {
            // 未知异常（如数据库不可用）：记录日志后仍提交 offset，
            // 因为无限重试同一条消息会阻塞整个分区的后续消息；失败状态可通过状态接口人工排查
            log.error("文档管理_消息消费异常, payload={}", payload, e);
            ack.acknowledge();
        }
    }
}
