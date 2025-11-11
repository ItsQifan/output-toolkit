package com.zhouchuanxiang.outputtoolkit.codegenerator.xmlconfig;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

/**
 * @Author zhouchuanxiang
 * @Description  单表的配置
 * @Date 11:29 2025/9/26
 * @Param
 * @return
 **/
@Data
public class TableConfig {

    /**
     * 开关 true打开：会执行生成
     */
    @JacksonXmlProperty(isAttribute = true)
    private boolean genSwitch;
    /**
     * 作者
     */
    @JacksonXmlProperty(isAttribute = true)
    private String authorName;
    /**
     * 模式  模式：sql-根据建表语句生成代码
     */
    @JacksonXmlProperty(isAttribute = true)
    private String model;
    /**
     * 建表语句
     */
    @JacksonXmlProperty(isAttribute = true)
    private String sql;
    /**
     * 建表语句表名忽略的前缀
     */
    @JacksonXmlProperty(isAttribute = true)
    private String sqlIgnorePrefix;
    /**
     * dto的后缀，如DTO还是DO等
     */
    @JacksonXmlProperty(isAttribute = true)
    private String dtoSuffix;
    /**
     * mapper的后缀，如Mapper还是Dao等
     */
    @JacksonXmlProperty(isAttribute = true)
    private String mapperSuffix;
    /**
     * manager的后缀，如Manager还是Service
     */
    @JacksonXmlProperty(isAttribute = true)
    private String managerSuffix;
    /**
     * dto所在包名
     */
    @JacksonXmlProperty(isAttribute = true)
    private String dtoPackageName;
    /**
     * vo所在包名
     */
    @JacksonXmlProperty(isAttribute = true)
    private String voPackageName;
    /**
     * mapper所在包名
     */
    @JacksonXmlProperty(isAttribute = true)
    private String mapperPackageName;
    /**
     * manager所在包名
     */
    @JacksonXmlProperty(isAttribute = true)
    private String managerPackageName;
    /**
     * controller所在包名
     */
    @JacksonXmlProperty(isAttribute = true)
    private String controllerPackageName;
    /**
     * dto输出本地路径
     */
    @JacksonXmlProperty(isAttribute = true)
    private String dtoLocalPath;
    /**
     * vo输出本地路径
     */
    @JacksonXmlProperty(isAttribute = true)
    private String voLocalPath;
    /**
     * mapper输出本地路径
     */
    @JacksonXmlProperty(isAttribute = true)
    private String mapperLocalPath;
    /**
     * manager输出本地路径
     */
    @JacksonXmlProperty(isAttribute = true)
    private String managerLocalPath;
    /**
     * controller输出本地路径
     */
    @JacksonXmlProperty(isAttribute = true)
    private String controllerLocalPath;

}
