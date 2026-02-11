package com.zhouchuanxiang.outputtoolkit.justopen;

import com.zhouchuanxiang.outputtoolkit.justopen.fx.QuickLauncherApp;
import javafx.application.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 快捷启动器主程序
 *
 * 功能：
 * 1. 按快捷键在屏幕中央显示菜单
 * 2. 快速打开常用软件和网址
 * 3. 支持全局快捷键唤醒（默认 Ctrl+1）
 * 4. JSON配置文件管理软件和网址列表
 */
public class LauncherApplication {

    private static final Logger logger = LoggerFactory.getLogger(LauncherApplication.class);

    public static void main(String[] args) {
        logger.info("Quick Launcher starting...");

        // 启动 JavaFX 应用
        Application.launch(QuickLauncherApp.class, args);
    }
}
