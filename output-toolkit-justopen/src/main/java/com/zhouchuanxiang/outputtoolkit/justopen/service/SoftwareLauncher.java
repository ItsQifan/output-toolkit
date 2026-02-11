package com.zhouchuanxiang.outputtoolkit.justopen.service;

import com.zhouchuanxiang.outputtoolkit.justopen.config.SoftwareGroup;
import com.zhouchuanxiang.outputtoolkit.justopen.config.SoftwareItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 软件启动服务
 * 负责启动配置中的软件
 */
public class SoftwareLauncher {
    
    private static final Logger logger = LoggerFactory.getLogger(SoftwareLauncher.class);
    
    /**
     * 启动单个软件
     *
     * @param item 软件配置项
     * @return 是否启动成功
     */
    public boolean launch(SoftwareItem item) {
        if (item == null || item.getPath() == null || item.getPath().isBlank()) {
            logger.warn("软件路径为空，无法启动");
            return false;
        }
        
        String path = item.getPath();
        logger.info("正在启动软件: {} ({})", item.getName(), path);
        
        try {
            ProcessBuilder pb;
            
            // 检查是否是完整路径
            File file = new File(path);
            if (file.exists()) {
                // 完整路径，设置工作目录为软件所在目录
                pb = new ProcessBuilder(path);
                pb.directory(file.getParentFile());
            } else {
                // 可能是系统命令（如 notepad.exe, calc.exe）
                pb = new ProcessBuilder(path);
            }
            
            // 不等待进程结束
            pb.inheritIO();
            pb.start();
            
            logger.info("软件启动成功: {}", item.getName());
            return true;
        } catch (IOException e) {
            logger.error("启动软件失败: {} - {}", item.getName(), e.getMessage());
            return false;
        }
    }
    
    /**
     * 启动一个分组内的所有软件
     *
     * @param group 软件分组
     * @return 成功启动的数量
     */
    public int launchGroup(SoftwareGroup group) {
        if (group == null || group.getItems() == null) {
            return 0;
        }
        
        logger.info("正在启动分组: {}", group.getName());
        int successCount = 0;
        
        for (SoftwareItem item : group.getItems()) {
            if (launch(item)) {
                successCount++;
            }
            // 稍微延迟，避免同时启动太多程序
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        logger.info("分组 {} 启动完成，成功: {}/{}", 
                group.getName(), successCount, group.getItems().size());
        return successCount;
    }
    
    /**
     * 启动所有分组的所有软件
     *
     * @param groups 软件分组列表
     * @return 成功启动的总数量
     */
    public int launchAll(List<SoftwareGroup> groups) {
        if (groups == null) {
            return 0;
        }
        
        int totalSuccess = 0;
        for (SoftwareGroup group : groups) {
            totalSuccess += launchGroup(group);
        }
        return totalSuccess;
    }
}
