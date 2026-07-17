-- ==========================================
-- Agent RAG Demo 数据库初始化脚本
-- 包含：会话表、消息表、模拟订单表
-- ==========================================

-- 会话表：每个用户可创建多个独立对话
CREATE TABLE IF NOT EXISTS t_conversation (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '会话ID',
    user_id     VARCHAR(64)  NOT NULL DEFAULT 'default' COMMENT '用户标识',
    title       VARCHAR(256) NOT NULL DEFAULT '新对话' COMMENT '会话标题（自动取首条问题前30字）',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 1=进行中 0=已归档',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话表';

-- 消息表：记录每轮对话的完整交互（含工具调用）
CREATE TABLE IF NOT EXISTS t_message (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
    conversation_id BIGINT       NOT NULL COMMENT '所属会话ID',
    role            VARCHAR(16)  NOT NULL COMMENT '角色: system/user/assistant/tool',
    content         MEDIUMTEXT   NOT NULL COMMENT '消息内容（Markdown格式）',
    tool_name       VARCHAR(64)  DEFAULT NULL COMMENT '工具名称（仅tool角色有值）',
    token_count     INT          DEFAULT NULL COMMENT '预估token消耗数',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- 模拟订单表：供 OrderQueryTool 查询演示
CREATE TABLE IF NOT EXISTS t_order (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
    order_no      VARCHAR(32)   NOT NULL UNIQUE COMMENT '订单编号',
    product_name  VARCHAR(128)  NOT NULL COMMENT '商品名称',
    category      VARCHAR(64)   NOT NULL COMMENT '商品分类',
    quantity      INT           NOT NULL DEFAULT 1 COMMENT '购买数量',
    unit_price    DECIMAL(10,2) NOT NULL COMMENT '商品单价',
    total_amount  DECIMAL(12,2) NOT NULL COMMENT '订单总金额',
    customer_name VARCHAR(64)   NOT NULL COMMENT '客户姓名',
    status        VARCHAR(16)   NOT NULL DEFAULT 'completed' COMMENT '订单状态: pending/paid/shipped/completed/cancelled',
    created_at    DATETIME      NOT NULL COMMENT '下单时间',
    INDEX idx_created_at (created_at),
    INDEX idx_category (category),
    INDEX idx_status (status),
    INDEX idx_customer_name (customer_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模拟业务订单表';

-- 文档状态表：追踪 Kafka 异步文档处理流水线状态
-- 文档上传后插入 PENDING 状态，消费者处理完成后更新为 COMPLETED 或 FAILED
CREATE TABLE IF NOT EXISTS t_document (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '文档ID',
    filename      VARCHAR(256)  NOT NULL COMMENT '文档文件名',
    file_path     VARCHAR(512)  NOT NULL COMMENT '文件在服务器上的存储路径',
    status        VARCHAR(16)   NOT NULL DEFAULT 'PENDING' COMMENT '处理状态: PENDING/PROCESSING/COMPLETED/FAILED',
    chunk_count   INT           DEFAULT 0 COMMENT '文档分块数量（处理完成后填充）',
    error_msg     VARCHAR(1024) DEFAULT NULL COMMENT '处理失败的异常信息',
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    updated_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档处理状态表';

-- 风控图谱实体表：模拟风控领域的用户、设备、商户、IP等实体
CREATE TABLE IF NOT EXISTS t_graph_entity (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '实体ID',
    entity_name   VARCHAR(128)  NOT NULL COMMENT '实体名称（如"用户U1001"）',
    entity_type   VARCHAR(32)   NOT NULL COMMENT '实体类型: USER/DEVICE/MERCHANT/IP',
    description   VARCHAR(256)  DEFAULT NULL COMMENT '实体描述',
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_entity_type (entity_type),
    INDEX idx_entity_name (entity_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风控图谱实体表';

-- 风控图谱关系表：记录实体之间的关联关系
CREATE TABLE IF NOT EXISTS t_graph_relation (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关系ID',
    source_entity   VARCHAR(128)  NOT NULL COMMENT '源实体名称',
    target_entity   VARCHAR(128)  NOT NULL COMMENT '目标实体名称',
    relation_type   VARCHAR(32)   NOT NULL COMMENT '关系类型: LOGIN_FROM/TRADE_AT/BIND_DEVICE/USE_IP',
    weight          DOUBLE        DEFAULT 1.0 COMMENT '关系权重（业务含义：关联强度）',
    description     VARCHAR(256)  DEFAULT NULL COMMENT '关系描述',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_source (source_entity),
    INDEX idx_target (target_entity),
    INDEX idx_relation_type (relation_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风控图谱关系表';
