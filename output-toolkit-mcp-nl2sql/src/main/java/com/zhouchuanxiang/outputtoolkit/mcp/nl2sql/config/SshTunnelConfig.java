package com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * SSH 隧道配置
 * <p>
 * 映射 MYSQL_SSH_* 环境变量，用于通过跳板机建立 SSH 隧道连接数据库。
 * </p>
 *
 * @author qifan
 * @since 2026-07-09
 */
@Data
@Component
@ConfigurationProperties(prefix = "mysql.ssh")
public class SshTunnelConfig {

    /** 是否启用 SSH 隧道 */
    private boolean enabled = false;

    /** SSH 跳板机主机地址 */
    private String host;

    /** SSH 端口，默认 22 */
    private int port = 22;

    /** SSH 登录用户名 */
    private String user;

    /** SSH 私钥文件路径 */
    private String privateKey;

    /** SSH 密码（与私钥二选一） */
    private String password;

    /** SSH 连接超时（秒），默认 10 */
    private int connectTimeout = 10;
}
