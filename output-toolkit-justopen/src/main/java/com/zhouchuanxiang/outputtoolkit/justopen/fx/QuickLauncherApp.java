package com.zhouchuanxiang.outputtoolkit.justopen.fx;

import com.zhouchuanxiang.outputtoolkit.justopen.config.ConfigLoader;
import com.zhouchuanxiang.outputtoolkit.justopen.config.LauncherConfig;
import com.zhouchuanxiang.outputtoolkit.justopen.hotkey.GlobalHotkeyManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JavaFX 快捷启动器主应用
 * 按快捷键在屏幕中央显示菜单
 */
public class QuickLauncherApp extends Application {
    
    private static final Logger logger = LoggerFactory.getLogger(QuickLauncherApp.class);
    
    private ConfigLoader configLoader;
    private LauncherConfig config;
    private GlobalHotkeyManager hotkeyManager;
    private LauncherMenuWindow menuWindow;
    
    @Override
    public void init() {
        // 在 JavaFX 应用线程启动前初始化
        configLoader = new ConfigLoader();
        config = configLoader.loadConfig();
        hotkeyManager = new GlobalHotkeyManager();
    }
    
    @Override
    public void start(Stage primaryStage) {
        logger.info("Quick Launcher starting...");
        
        // 设置 JavaFX 在所有窗口关闭后不退出（因为我们是后台运行）
        Platform.setImplicitExit(false);
        
        // 创建菜单窗口
        menuWindow = new LauncherMenuWindow(config, configLoader, this::reloadConfig, this::shutdown);
        
        // 初始化全局快捷键
        initializeHotkey();
        
        // 显示菜单（首次启动时显示）
        menuWindow.showAtCenter();
        
        logger.info("Quick Launcher started. Press {} to show menu.", config.getHotkey());
    }
    
    /**
     * 初始化全局快捷键
     */
    private void initializeHotkey() {
        String hotkey = config.getHotkey();
        if (hotkey == null || hotkey.isBlank()) {
            hotkey = "CTRL+1";
        }
        
        hotkeyManager.setHotkeyConfig(hotkey);
        hotkeyManager.setHotkeyCallback(this::onHotkeyTriggered);
        hotkeyManager.register();
        
        logger.info("Hotkey registered: {}", hotkey);
    }
    
    /**
     * 快捷键触发时的处理
     */
    private void onHotkeyTriggered() {
        logger.info("Hotkey triggered");
        Platform.runLater(() -> {
            if (menuWindow.isShowing()) {
                menuWindow.hide();
            } else {
                menuWindow.showAtCenter();
            }
        });
    }
    
    /**
     * 重新加载配置
     */
    private void reloadConfig() {
        config = configLoader.reloadConfig();
        menuWindow.updateConfig(config);
        
        // 更新快捷键
        hotkeyManager.setHotkeyConfig(config.getHotkey());
        
        logger.info("Config reloaded. Hotkey: {}", config.getHotkey());
    }
    
    /**
     * 关闭应用
     */
    private void shutdown() {
        logger.info("Shutting down...");
        hotkeyManager.unregister();
        Platform.exit();
        System.exit(0);
    }
    
    @Override
    public void stop() {
        logger.info("Application stopping...");
        if (hotkeyManager != null) {
            hotkeyManager.unregister();
        }
    }
}
