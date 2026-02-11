package com.zhouchuanxiang.outputtoolkit.justopen.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 网址分组配置
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UrlGroup {
    
    /**
     * 分组名称（如：工作网站、常用工具）
     */
    private String name;
    
    /**
     * 该分组下的网址列表
     */
    private List<UrlItem> items;
}
