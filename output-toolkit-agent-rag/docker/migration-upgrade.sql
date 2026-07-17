-- ==========================================
-- 本次改造的增量初始化脚本（只含新增表和图谱数据）
-- 在已有 agent_rag_db 上执行，不影响存量表
-- ==========================================

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

CREATE TABLE IF NOT EXISTS t_graph_entity (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '实体ID',
    entity_name   VARCHAR(128)  NOT NULL COMMENT '实体名称（如"用户U1001"）',
    entity_type   VARCHAR(32)   NOT NULL COMMENT '实体类型: USER/DEVICE/MERCHANT/IP',
    description   VARCHAR(256)  DEFAULT NULL COMMENT '实体描述',
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_entity_type (entity_type),
    INDEX idx_entity_name (entity_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风控图谱实体表';

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

-- 幂等清理后重新插入图谱模拟数据
DELETE FROM t_graph_relation;
DELETE FROM t_graph_entity;

INSERT INTO t_graph_entity (entity_name, entity_type, description) VALUES
('用户U1001',        'USER',     '注册2年的正常用户，实名认证'),
('用户U1002',        'USER',     '新注册用户，未实名'),
('用户U1003',        'USER',     '新注册用户，命中营销羊毛党名单'),
('用户U1004',        'USER',     '新注册用户，注册后立即交易'),
('用户U1005',        'USER',     '休眠账户近期突然活跃'),
('设备D2001',        'DEVICE',   'iPhone 15，用户U1001 常用设备'),
('设备D2002',        'DEVICE',   '安卓模拟器特征设备，疑似猫池'),
('商户M3001',        'MERCHANT', '连锁便利店，正常商户'),
('商户M3002',        'MERCHANT', '新入网商户，费率异常，疑似套现商户'),
('IP:192.168.1.100', 'IP',       '家庭宽带IP，归属地上海'),
('IP:10.8.8.8',      'IP',       '机房代理IP，归属地频繁变动');

INSERT INTO t_graph_relation (source_entity, target_entity, relation_type, weight, description) VALUES
('用户U1001', '设备D2001',        'BIND_DEVICE', 1.0, '常用设备，绑定2年'),
('用户U1001', 'IP:192.168.1.100', 'USE_IP',      1.0, '常用登录IP'),
('用户U1001', '商户M3001',        'TRADE_AT',    0.8, '近30天交易12笔，金额正常'),
('用户U1002', '设备D2002',        'BIND_DEVICE', 0.9, '注册即绑定'),
('用户U1003', '设备D2002',        'BIND_DEVICE', 0.9, '注册即绑定'),
('用户U1004', '设备D2002',        'BIND_DEVICE', 0.9, '注册即绑定'),
('用户U1003', 'IP:10.8.8.8',      'USE_IP',      0.9, '近7天固定使用'),
('用户U1004', 'IP:10.8.8.8',      'USE_IP',      0.9, '近7天固定使用'),
('用户U1005', 'IP:10.8.8.8',      'USE_IP',      0.8, '近3天开始使用'),
('用户U1003', '商户M3002',        'TRADE_AT',    0.95, '近7天大额整数交易8笔'),
('用户U1004', '商户M3002',        'TRADE_AT',    0.95, '近7天大额整数交易6笔'),
('用户U1005', '商户M3002',        'TRADE_AT',    0.9,  '近3天大额整数交易4笔');
