package com.zhouchuanxiang.outputtoolkit.justopen.service;

import com.zhouchuanxiang.outputtoolkit.justopen.config.UrlGroup;
import com.zhouchuanxiang.outputtoolkit.justopen.config.UrlItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * 网址打开服务
 * 负责使用浏览器打开配置中的网址
 */
public class UrlOpener {
    
    private static final Logger logger = LoggerFactory.getLogger(UrlOpener.class);
    
    /**
     * 配置的浏览器路径（为空则使用系统默认浏览器）
     */
    private String browserPath;
    
    public UrlOpener() {
        this.browserPath = null;
    }
    
    public UrlOpener(String browserPath) {
        this.browserPath = browserPath;
    }
    
    /**
     * 设置浏览器路径
     */
    public void setBrowserPath(String browserPath) {
        this.browserPath = browserPath;
    }
    
    /**
     * 打开单个网址
     *
     * @param item 网址配置项
     * @return 是否打开成功
     */
    public boolean open(UrlItem item) {
        if (item == null || item.getUrl() == null || item.getUrl().isBlank()) {
            logger.warn("网址为空，无法打开");
            return false;
        }
        
        String url = item.getUrl();
        logger.info("正在打开网址: {} ({})", item.getName(), url);
        
        try {
            if (browserPath != null && !browserPath.isBlank()) {
                // 使用配置的浏览器
                return openWithBrowser(url);
            } else {
                // 使用系统默认浏览器
                return openWithDefaultBrowser(url);
            }
        } catch (Exception e) {
            logger.error("打开网址失败: {} - {}", item.getName(), e.getMessage());
            return false;
        }
    }
    
    /**
     * 使用配置的浏览器打开网址
     */
    private boolean openWithBrowser(String url) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(browserPath, url);
        pb.start();
        logger.info("使用浏览器打开网址成功: {}", url);
        return true;
    }
    
    /**
     * 使用系统默认浏览器打开网址
     */
    private boolean openWithDefaultBrowser(String url) throws IOException, URISyntaxException {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(new URI(url));
            logger.info("使用默认浏览器打开网址成功: {}", url);
            return true;
        } else {
            // 如果Desktop不支持，尝试使用系统命令
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;
            
            if (os.contains("win")) {
                pb = new ProcessBuilder("cmd", "/c", "start", url);
            } else if (os.contains("mac")) {
                pb = new ProcessBuilder("open", url);
            } else {
                pb = new ProcessBuilder("xdg-open", url);
            }
            
            pb.start();
            logger.info("使用系统命令打开网址成功: {}", url);
            return true;
        }
    }
    
    /**
     * 打开一个分组内的所有网址
     *
     * @param group 网址分组
     * @return 成功打开的数量
     */
    public int openGroup(UrlGroup group) {
        if (group == null || group.getItems() == null) {
            return 0;
        }
        
        logger.info("正在打开网址分组: {}", group.getName());
        int successCount = 0;
        
        for (UrlItem item : group.getItems()) {
            if (open(item)) {
                successCount++;
            }
            // 稍微延迟，避免同时打开太多标签页
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        logger.info("网址分组 {} 打开完成，成功: {}/{}", 
                group.getName(), successCount, group.getItems().size());
        return successCount;
    }
    
    /**
     * 打开所有分组的所有网址
     *
     * @param groups 网址分组列表
     * @return 成功打开的总数量
     */
    public int openAll(List<UrlGroup> groups) {
        if (groups == null) {
            return 0;
        }
        
        int totalSuccess = 0;
        for (UrlGroup group : groups) {
            totalSuccess += openGroup(group);
        }
        return totalSuccess;
    }
}
