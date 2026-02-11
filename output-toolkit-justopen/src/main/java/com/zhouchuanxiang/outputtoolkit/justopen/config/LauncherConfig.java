package com.zhouchuanxiang.outputtoolkit.justopen.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 启动器主配置
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LauncherConfig {
    
    /**
     * 浏览器可执行文件路径
     * 如果为空，则使用系统默认浏览器
     */
    private String browser;
    
    /**
     * 全局快捷键配置（如：CTRL+1）
     */
    private String hotkey = "CTRL+1";
    
    /**
     * 软件分组列表
     */
    private List<SoftwareGroup> softwareGroups;
    
    /**
     * 网址分组列表
     */
    private List<UrlGroup> urlGroups;
}
