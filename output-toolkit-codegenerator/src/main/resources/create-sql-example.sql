### 批量生成示例SQL文件
### 可以一次放多个CREATE TABLE语句，系统会自动批量处理
### 复制需要的建表语句到 create-sql.sql 文件中使用

-- ============================================
-- 示例1：用户信息表
-- ===========================================
CREATE TABLE `t_p_user_info` (
  `id` varchar(32) NOT NULL COMMENT '用户ID',
  `user_name` varchar(100) NOT NULL COMMENT '用户名',
  `phone` varchar(20) COMMENT '手机号',
  `email` varchar(100) COMMENT '邮箱',
  `status` varchar(32) DEFAULT 'active' COMMENT '状态：active-激活，inactive-未激活',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `updated_at` datetime DEFAULT NULL COMMENT '更新时间',
  `updated_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`)
) COMMENT = '用户信息表';

-- ============================================
-- 示例2：用户角色关联表
-- ===========================================
CREATE TABLE `t_p_user_role_rel` (
  `id` varchar(32) NOT NULL COMMENT 'ID',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `role_id` varchar(32) NOT NULL COMMENT '角色ID',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  PRIMARY KEY (`id`)
) COMMENT = '用户角色关联表';

-- ============================================
-- 示例3：订单信息表
-- ===========================================
CREATE TABLE `t_p_order_info` (
  `id` varchar(32) NOT NULL COMMENT '订单ID',
  `order_no` varchar(64) NOT NULL COMMENT '订单号',
  `user_id` varchar(32) NOT NULL COMMENT '用户ID',
  `amount` decimal(10,2) NOT NULL COMMENT '订单金额',
  `status` varchar(32) NOT NULL COMMENT '订单状态',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `updated_at` datetime DEFAULT NULL COMMENT '更新时间',
  `updated_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`)
) COMMENT = '订单信息表';

-- ============================================
-- 示例4：当前项目示例（图谱权限申请表）
-- ===========================================
CREATE TABLE `t_p_neb_info_auth` (
  `ID` varchar(32) NOT NULL COMMENT 'id',
  `GRAPH_NAME` varchar(255) NOT NULL COMMENT '图谱名称',
  `GRAPH_NUMBER` varchar(100) NOT NULL COMMENT '图谱编码',
  `CREATOR` varchar(32) NOT NULL COMMENT '创建者账户名或ID',
  `STATUS` varchar(32) NOT NULL COMMENT '申请状态，待处理pending；已完成 completed；已拒绝 refused',
  `CREATED_AT` datetime DEFAULT NULL COMMENT '创建时间 申请时间',
  `CREATED_BY` varchar(32) DEFAULT NULL COMMENT '创建人 申请人',
  `UPDATED_AT` datetime DEFAULT NULL COMMENT '更新时间 审批时间',
  `UPDATED_BY` varchar(32) DEFAULT NULL COMMENT '更新人 审批人',
  PRIMARY KEY (`id`)
) COMMENT = '图谱权限申请表';
