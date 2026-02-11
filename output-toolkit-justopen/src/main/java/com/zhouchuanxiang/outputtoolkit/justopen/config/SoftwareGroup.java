package com.zhouchuanxiang.outputtoolkit.justopen.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 软件分组配置
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SoftwareGroup {
    
    /**
     * 分组名称（如：开发工具、办公软件）
     */
    private String name;
    
    /**
     * 该分组下的软件列表
     */
    private List<SoftwareItem> items;
}
