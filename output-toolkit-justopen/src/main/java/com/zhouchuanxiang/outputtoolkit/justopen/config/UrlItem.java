package com.zhouchuanxiang.outputtoolkit.justopen.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 网址项配置
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UrlItem {
    
    /**
     * 网址名称（显示在菜单中）
     */
    private String name;
    
    /**
     * 网址URL
     */
    private String url;
}
