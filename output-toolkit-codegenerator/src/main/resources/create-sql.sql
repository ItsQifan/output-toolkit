CREATE TABLE `t_p_neb_info_auth` (
                                     `ID` varchar(32)  NOT NULL COMMENT 'id',
                                     `GRAPH_NAME` varchar(255)  NOT NULL COMMENT '图谱名称',
                                     `GRAPH_NUMBER` varchar(100)  NOT NULL COMMENT '图谱编码',
                                     `CREATOR` varchar(32)  NOT NULL COMMENT '创建者账户名或ID',
                                     `STATUS` varchar(32)  NOT NULL COMMENT '申请状态，待处理pending；已完成 completed；已拒绝 refused',
                                     `CREATED_AT` datetime DEFAULT NULL COMMENT '创建时间 申请时间',
                                     `CREATED_BY` varchar(32)  DEFAULT NULL COMMENT '创建人 申请人',
                                     `UPDATED_AT` datetime DEFAULT NULL COMMENT '更新时间 审批时间',
                                     `UPDATED_BY` varchar(32)  DEFAULT NULL COMMENT '更新人 审批人',
                                     PRIMARY KEY (`id`)
) COMMENT = '图谱权限申请表';
