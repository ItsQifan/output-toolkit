package com.zhouchuanxiang.outputtoolkit.justopen.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 配置加载器
 * 负责加载和保存配置文件
 */
public class ConfigLoader {

    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);

    /**
     * 配置文件名
     */
    private static final String CONFIG_FILE_NAME = "launcher-config.json";

    /**
     * 用户配置目录（在用户主目录下）
     */
    private static final String CONFIG_DIR_NAME = ".output-toolkit-justopen";

    private final ObjectMapper objectMapper;
    private final Path configFilePath;
    private LauncherConfig config;

    public ConfigLoader() {
        this.objectMapper = new ObjectMapper();
        this.configFilePath = getConfigFilePath();
    }

    /**
     * 获取配置文件路径
     * 优先使用用户目录下的配置文件
     */
    private Path getConfigFilePath() {
        // 首先检查当前工作目录
        Path currentDirConfig = Paths.get(CONFIG_FILE_NAME);
        if (Files.exists(currentDirConfig)) {
            return currentDirConfig;
        }

        // 然后检查用户目录
        String userHome = System.getProperty("user.home");
        Path userConfigDir = Paths.get(userHome, CONFIG_DIR_NAME);
        return userConfigDir.resolve(CONFIG_FILE_NAME);
    }

    /**
     * 加载配置
     * 如果配置文件不存在，则创建默认配置
     */
    public LauncherConfig loadConfig() {
        if (Files.exists(configFilePath)) {
            try {

                // Read raw content for debugging
                String rawContent = Files.readString(configFilePath);


                config = objectMapper.readValue(configFilePath.toFile(), LauncherConfig.class);



                logger.info("配置文件加载成功: {}", configFilePath);
                return config;
            } catch (IOException e) {

                logger.error("加载配置文件失败: {}", e.getMessage());
            }
        }

        // 配置文件不存在，创建默认配置
        logger.info("配置文件不存在，创建默认配置: {}", configFilePath);
        config = createDefaultConfig();
        saveConfig(config);
        return config;
    }

    /**
     * 重新加载配置
     */
    public LauncherConfig reloadConfig() {
        logger.info("重新加载配置文件...");
        return loadConfig();
    }

    /**
     * 保存配置到文件
     */
    public void saveConfig(LauncherConfig config) {
        try {
            // 确保目录存在
            Path parentDir = configFilePath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }



            String jsonContent = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(config);


            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(configFilePath.toFile(), config);
            logger.info("配置文件保存成功: {}", configFilePath);
        } catch (IOException e) {
            logger.error("保存配置文件失败: {}", e.getMessage());
        }
    }

    /**
     * 创建默认配置
     */
    private LauncherConfig createDefaultConfig() {
        LauncherConfig defaultConfig = new LauncherConfig();
        defaultConfig.setBrowser("");  // 空表示使用系统默认浏览器
        defaultConfig.setHotkey("CTRL+1");
        defaultConfig.setSoftwareGroups(java.util.List.of(
                new SoftwareGroup("开发工具", java.util.List.of(
                        new SoftwareItem("记事本", "C:\\Windows\\System32\\notepad.exe"),
                        new SoftwareItem("计算器", "C:\\Windows\\System32\\calc.exe"),
                        new SoftwareItem("VS Code", "C:\\Users\\用户名\\AppData\\Local\\Programs\\Microsoft VS Code\\Code.exe"),
                        new SoftwareItem("IDEA", "D:\\software\\IntelliJ IDEA\\bin\\idea64.exe")
                )),
                new SoftwareGroup("远程工具", java.util.List.of(
                        new SoftwareItem("Xshell", "D:\\software\\Xshell\\Xshell.exe"),
                        new SoftwareItem("Xftp", "D:\\software\\Xftp\\Xftp.exe")
                )),
                new SoftwareGroup("办公软件", java.util.List.of(
                        new SoftwareItem("画图", "C:\\Windows\\System32\\mspaint.exe")
                ))
        ));
        defaultConfig.setUrlGroups(java.util.List.of(
                new UrlGroup("常用网站", java.util.List.of(
                        new UrlItem("百度", "https://www.baidu.com"),
                        new UrlItem("Google", "https://www.google.com")
                )),
                new UrlGroup("开发资源", java.util.List.of(
                        new UrlItem("GitHub", "https://github.com"),
                        new UrlItem("Stack Overflow", "https://stackoverflow.com")
                ))
        ));
        return defaultConfig;
    }

    /**
     * 获取当前配置
     */
    public LauncherConfig getConfig() {
        if (config == null) {
            config = loadConfig();
        }
        return config;
    }

    /**
     * 获取配置文件路径（用于在编辑器中打开）
     */
    public Path getConfigFilePathForEdit() {
        return configFilePath;
    }

    /**
     * 用默认编辑器打开配置文件
     */
    public void openConfigInEditor() {
        try {
            File configFile = configFilePath.toFile();
            if (!configFile.exists()) {
                saveConfig(createDefaultConfig());
            }

            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                // Windows: 使用 notepad 打开
                Runtime.getRuntime().exec(new String[]{"notepad.exe", configFile.getAbsolutePath()});
                logger.info("已用记事本打开配置文件: {}", configFile.getAbsolutePath());
            } else if (os.contains("mac")) {
                // macOS: 使用 open 命令
                Runtime.getRuntime().exec(new String[]{"open", "-t", configFile.getAbsolutePath()});
                logger.info("已打开配置文件: {}", configFile.getAbsolutePath());
            } else {
                // Linux: 尝试使用 xdg-open
                Runtime.getRuntime().exec(new String[]{"xdg-open", configFile.getAbsolutePath()});
                logger.info("已打开配置文件: {}", configFile.getAbsolutePath());
            }
        } catch (IOException e) {
            logger.error("打开配置文件失败: {}", e.getMessage());
            // 备用方案：尝试使用 Desktop.open()
            try {
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().open(configFilePath.toFile());
                }
            } catch (IOException ex) {
                logger.error("备用方案也失败: {}", ex.getMessage());
            }
        }
    }
}
