package com.zhouchuanxiang.outputtoolkit.justopen.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 软件项配置
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SoftwareItem {
    
    /**
     * 软件名称（显示在菜单中）
     */
    private String name;
    
    /**
     * 软件可执行文件路径
     */
    private String path;
}
