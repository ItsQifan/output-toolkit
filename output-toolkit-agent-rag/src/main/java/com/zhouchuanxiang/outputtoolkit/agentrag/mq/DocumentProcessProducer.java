package com.zhouchuanxiang.outputtoolkit.agentrag.mq;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 文档处理消息生产者
 * <p>
 * 文档上传接口通过此生产者发送处理消息到 Kafka，
 * 实现"文档接收"与"切分/向量化/入库"两个阶段的异步解耦：
 * 上传接口毫秒级返回，耗时的 Embedding 计算由消费者后台完成。
 * </p>
 * <p>
 * 使用原因：Embedding 向量化需要逐块调用远程模型 API，大文档处理耗时可达分钟级。
 * 若同步处理，上传接口会长时间阻塞且失败后无法重试；
 * 通过 Kafka 解耦后，消息持久化在 Broker 中，消费失败可重新消费，天然具备削峰与重试能力。
 * </p>
 *
 * @author qifan
 * @since 2026-07-16
 */
//tips: 为什么用 Kafka 而不是线程池异步？两者都能实现"异步"，但 Kafka 多了三个保障：
//     1. 持久化：消息落盘，应用重启后未处理的消息不会丢（线程池任务重启即丢）。
//     2. 削峰：批量上传100个文档时，消息在 Broker 排队，消费者按自己的节奏消费。
//     3. 解耦：未来可以把"文档处理"拆成独立服务水平扩容，生产方代码零改动。
@Slf4j
@Component
public class DocumentProcessProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    /** 文档处理消息主题（配置项 agent-rag.kafka.doc-process-topic） */
    private final String docProcessTopic;

    public DocumentProcessProducer(KafkaTemplate<String, String> kafkaTemplate,
                                   @Value("${agent-rag.kafka.doc-process-topic}") String docProcessTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.docProcessTopic = docProcessTopic;
    }

    /**
     * 发送文档处理消息
     * <p>
     * 使用 documentId 作为消息 Key，保证同一文档的消息进入同一分区，
     * 避免同一文档被并发处理导致的向量重复写入。
     * </p>
     *
     * @param message 文档处理消息
     */
    public void sendDocProcessMessage(DocumentProcessMessage message) {
        String payload = JSON.toJSONString(message);
        // whenComplete 异步回调记录发送结果，不阻塞上传接口响应
        kafkaTemplate.send(docProcessTopic, String.valueOf(message.getDocumentId()), payload)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("文档管理_Kafka消息发送失败, documentId={}, filename={}",
                                message.getDocumentId(), message.getFilename(), ex);
                    } else {
                        log.info("文档管理_Kafka消息发送成功, documentId={}, topic={}, partition={}, offset={}",
                                message.getDocumentId(), docProcessTopic,
                                result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                    }
                });
    }
}
