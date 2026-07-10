package com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.tunnel;

import com.zhouchuanxiang.outputtoolkit.mcp.nl2sql.config.SshTunnelConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * SSH 隧道生命周期管理器
 * <p>
 * 使用 Apache SSHD 实现纯 Java SSH 端口转发，不依赖系统 ssh 命令。
 * 支持通过跳板机建立隧道连接远程数据库。
 * </p>
 *
 * @author qifan
 * @since 2026-07-09
 */
@Slf4j
@Component
public class SshTunnelManager {

    private final SshTunnelConfig config;

    public SshTunnelManager(SshTunnelConfig config) {
        this.config = config;
    }

    /**
     * 获取连接信息（host + port）
     * <p>
     * Phase 1 为 stub 实现，直接返回数据库直连地址。
     * Phase 3 实现 SSH 隧道建立逻辑，返回隧道本地转发地址。
     * </p>
     *
     * @return 连接信息数组 [host, port]
     */
    public Object[] getConnectionInfo() {
        // TODO Phase 3: 判断是否启用 SSH 隧道，启用则建立隧道并返回本地转发地址
        return new Object[]{"localhost", 3306};
    }

    /**
     * 启动 SSH 隧道（Phase 3 实现）
     */
    private void start() {
        // TODO Phase 3: 使用 Apache SSHD 建立 SSH 端口转发
    }

    /**
     * 关闭 SSH 隧道（Phase 3 实现）
     * 通过 @PreDestroy 确保应用关闭时自动清理
     */
    private void stop() {
        // TODO Phase 3: 关闭 SSH 客户端和端口转发
    }
}
