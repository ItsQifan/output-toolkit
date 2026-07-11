package com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.db;

import com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.config.DbConfig;
import com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.config.SshTunnelConfig;
import com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.tunnel.SshTunnelManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库连接工厂（含 HikariCP 连接池）
 * <p>
 * 使用 HikariCP 连接池管理 MySQL 连接，支持：
 * <ul>
 *   <li>单库模式：DataSource 绑定 MYSQL_DATABASE</li>
 *   <li>多库模式：DataSource 不绑定数据库，运行时通过 database.table 切换</li>
 *   <li>SSL 四种模式：DISABLED / REQUIRED / VERIFY_CA / VERIFY_IDENTITY</li>
 *   <li>可选参数：charset、collation、sql_mode、connect_timeout、auth_plugin</li>
 * </ul>
 * </p>
 *
 * @author qifan
 * @since 2026-07-09
 */
@Slf4j
@Configuration
public class ConnectionFactory {

    private static final String JDBC_PROTOCOL = "jdbc:mysql:";

    /** MySQL 字符集 → Java 编码名映射 */
    private static final Map<String, String> CHARSET_MAPPING = new LinkedHashMap<>();

    static {
        CHARSET_MAPPING.put("utf8mb4", "UTF-8");
        CHARSET_MAPPING.put("utf8mb3", "UTF-8");
        CHARSET_MAPPING.put("utf8", "UTF-8");
        CHARSET_MAPPING.put("latin1", "ISO-8859-1");
        CHARSET_MAPPING.put("gbk", "GBK");
        CHARSET_MAPPING.put("gb2312", "GB2312");
        CHARSET_MAPPING.put("big5", "Big5");
        CHARSET_MAPPING.put("ujis", "EUC-JP");
        CHARSET_MAPPING.put("euckr", "EUC-KR");
    }

    private final DbConfig dbConfig;
    private final SshTunnelConfig sshTunnelConfig;
    private final SshTunnelManager sshTunnelManager;

    public ConnectionFactory(DbConfig dbConfig,
                             SshTunnelConfig sshTunnelConfig,
                             SshTunnelManager sshTunnelManager) {
        this.dbConfig = dbConfig;
        this.sshTunnelConfig = sshTunnelConfig;
        this.sshTunnelManager = sshTunnelManager;
    }

    /**
     * 创建 HikariCP DataSource Bean
     * <p>
     * 根据 DbConfig 动态构建连接池，支持 SSL、SSH 隧道、多库模式。
     * Spring Boot 的 DataSourceAutoConfiguration 会检测到此 Bean 后自动退避。
     * </p>
     *
     * @return HikariCP 数据源
     */
    @Bean
    public DataSource dataSource() {
        // Phase 2: 直接使用数据库地址（Phase 3 集成 SSH 隧道后改为通过 SshTunnelManager 获取）
        String actualHost = dbConfig.getHost();
        int actualPort = dbConfig.getPort();

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(buildJdbcUrl(actualHost, actualPort));
        hikariConfig.setUsername(dbConfig.getUser());
        hikariConfig.setPassword(dbConfig.getPassword());

        // 连接池基础配置
        hikariConfig.setMinimumIdle(1);
        hikariConfig.setMaximumPoolSize(10);
        // 启动时不验证连接（延迟到首次 SQL 调用时再建立连接）
        // SSE 模式下数据库凭证由 Claude Desktop 通过 env 传入，启动阶段可能不可用
        hikariConfig.setInitializationFailTimeout(-1);
        hikariConfig.setConnectionTimeout(dbConfig.getConnectTimeout() * 1000L);
        hikariConfig.setIdleTimeout(600000);       // 空闲连接 10 分钟后释放
        hikariConfig.setMaxLifetime(1800000);      // 连接最大存活 30 分钟
        hikariConfig.setConnectionTestQuery("SELECT 1");

        // 连接初始化 SQL（设置 sql_mode 等会话变量）
        String initSql = buildInitSql();
        if (initSql != null && !initSql.isEmpty()) {
            hikariConfig.setConnectionInitSql(initSql);
        }

        log.info("数据库连接_初始化连接池, host={}, port={}, database={}, sslMode={}",
                actualHost, actualPort,
                dbConfig.getDatabase() != null ? dbConfig.getDatabase() : "多库模式",
                dbConfig.getSsl().getMode());

        return new HikariDataSource(hikariConfig);
    }

    /**
     * 创建 JdbcTemplate Bean
     * <p>
     * Spring Boot 自动配置会基于我们提供的 DataSource 创建 JdbcTemplate。
     * 此处显式声明以便在其他 Service 中直接注入使用。
     * </p>
     *
     * @param dataSource HikariCP 数据源
     * @return JdbcTemplate 实例
     */
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        // 设置查询超时 30 秒
        jdbcTemplate.setQueryTimeout(30);
        return jdbcTemplate;
    }

    /**
     * 构建 JDBC URL
     * <p>
     * 拼接完整 JDBC URL，包含 SSL、charset、collation 等参数。
     * 多库模式下不拼接 database 路径。
     * </p>
     *
     * @param host 数据库主机地址
     * @param port 数据库端口
     * @return JDBC URL 字符串
     */
    private String buildJdbcUrl(String host, int port) {
        StringBuilder url = new StringBuilder(JDBC_PROTOCOL);
        // MySQL Connector/J 8.x 使用 mysqlx:// 协议？不，普通查询还是 mysql://
        url.append("//").append(host).append(":").append(port).append("/");

        // 单库模式：拼接数据库名
        String database = dbConfig.getDatabase();
        if (database != null && !database.isEmpty()) {
            url.append(database);
        }

        // 构建查询参数
        List<String> params = new ArrayList<>();

        // MySQL 8.0+ caching_sha2_password 认证需要允许公钥检索
        params.add("allowPublicKeyRetrieval=true");

        // SSL 模式映射（MySQL Connector/J 8.x 使用 sslMode 属性）
        String sslMode = dbConfig.getSsl().getMode();
        if (sslMode != null && !sslMode.isEmpty()) {
            params.add("sslMode=" + sslMode);
            // VERIFY_CA / VERIFY_IDENTITY 模式下需要配置证书路径（Phase 3 完善）
            if ("VERIFY_CA".equalsIgnoreCase(sslMode) || "VERIFY_IDENTITY".equalsIgnoreCase(sslMode)) {
                if (dbConfig.getSsl().getCa() != null) {
                    params.add("trustCertificateKeyStoreUrl=file:" + dbConfig.getSsl().getCa());
                }
            }
        }

        // 字符集 —— MySQL 字符集名需映射为 Java 编码名（如 utf8mb4 → UTF-8）
        String mysqlCharset = dbConfig.getCharset();
        String javaEncoding = mapCharsetToJavaEncoding(mysqlCharset);
        if (javaEncoding != null && !javaEncoding.isEmpty()) {
            params.add("characterEncoding=" + javaEncoding);
        }

        // 排序规则（MySQL Connector/J 使用 connectionCollation）
        String collation = dbConfig.getCollation();
        if (collation != null && !collation.isEmpty()) {
            params.add("connectionCollation=" + collation);
        }

        // 连接超时（毫秒）
        params.add("connectTimeout=" + (dbConfig.getConnectTimeout() * 1000));

        // 认证插件
        String authPlugin = dbConfig.getAuthPlugin();
        if (authPlugin != null && !authPlugin.isEmpty()) {
            params.add("defaultAuthenticationPlugin=" + authPlugin);
            // MySQL Connector/J 8.4+ 可能需要不同的属性名
            params.add("authenticationPlugins=" + authPlugin);
        }

        // 拼接参数
        if (!params.isEmpty()) {
            url.append("?").append(String.join("&", params));
        }

        return url.toString();
    }

    /**
     * 构建连接初始化 SQL
     * <p>
     * 在每次获取连接时执行，设置会话变量：
     * <ul>
     *   <li>sql_mode —— 数据库 SQL 模式</li>
     *   <li>SET NAMES —— MySQL 字符集（当 MySQL 名与 Java 编码不一致时）</li>
     * </ul>
     * </p>
     *
     * @return 初始化 SQL，无配置时返回 null
     */
    private String buildInitSql() {
        List<String> statements = new ArrayList<>();

        // sql_mode 设置
        String sqlMode = dbConfig.getSqlMode();
        if (sqlMode != null && !sqlMode.isEmpty()) {
            statements.add("SET SESSION sql_mode='" + sqlMode + "'");
        }

        // MySQL 字符集设置 —— 当 MySQL 名与 Java 编码名不一致时，需要 SET NAMES
        String mysqlCharset = dbConfig.getCharset();
        if (mysqlCharset != null && !mysqlCharset.isEmpty()) {
            String javaEncoding = CHARSET_MAPPING.get(mysqlCharset.toLowerCase());
            // 如果 MySQL 字符集名与 Java 编码名不同，需要用 SET NAMES 设置 MySQL 端字符集
            if (javaEncoding != null && !mysqlCharset.equalsIgnoreCase(javaEncoding)) {
                statements.add("SET NAMES " + mysqlCharset);
            }
        }

        if (statements.isEmpty()) {
            return null;
        }
        return String.join("; ", statements);
    }

    /**
     * MySQL 字符集名 → Java 编码名映射
     * <p>
     * MySQL Connector/J 的 characterEncoding 参数需要 Java 编码名称，
     * 而用户通常使用 MySQL 的字符集名（如 utf8mb4），此处做转换。
     * </p>
     *
     * @param mysqlCharset MySQL 字符集名
     * @return Java 编码名，无匹配时返回原始值
     */
    private String mapCharsetToJavaEncoding(String mysqlCharset) {
        if (mysqlCharset == null || mysqlCharset.isEmpty()) {
            return mysqlCharset;
        }
        return CHARSET_MAPPING.getOrDefault(mysqlCharset.toLowerCase(), mysqlCharset);
    }
}
