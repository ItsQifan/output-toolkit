package com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * 数据库连接配置
 * <p>
 * 映射所有 MYSQL_* 环境变量，包含 SSL 子配置。
 * 支持单库模式（指定 MYSQL_DATABASE）和多库模式（不指定数据库）。
 * </p>
 *
 * @author qifan
 * @since 2026-07-09
 */
@Data
@Component
@Validated
@ConfigurationProperties(prefix = "mysql")
public class DbConfig {

    /** 数据库主机地址，默认 localhost */
    private String host = "localhost";

    /** 数据库端口，默认 3306 */
    private int port = 3306;

    /** 数据库用户名 */
    @NotBlank(message = "数据库用户名不能为空")
    private String user;

    /** 数据库密码 */
    @NotBlank(message = "数据库密码不能为空")
    private String password;

    /** 默认数据库名（单库模式），为空则使用多库模式 */
    private String database;

    /** 字符集，默认 utf8mb4 */
    private String charset = "utf8mb4";

    /** 排序规则 */
    private String collation;

    /** SQL 模式 */
    private String sqlMode;

    /** 连接超时时间（秒），默认 10 */
    private int connectTimeout = 10;

    /** 认证插件 */
    private String authPlugin;

    /** SSL 配置 */
    private SslConfig ssl = new SslConfig();

    /**
     * SSL 子配置
     */
    @Data
    public static class SslConfig {
        /** SSL 模式：DISABLED / REQUIRED / VERIFY_CA / VERIFY_IDENTITY */
        private String mode = "DISABLED";

        /** CA 证书路径 */
        private String ca;

        /** 客户端证书路径 */
        private String cert;

        /** 客户端私钥路径 */
        private String key;
    }
}
