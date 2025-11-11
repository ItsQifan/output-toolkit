package com.zhouchuanxiang.outputtoolkit.codegenerator.entity;

import lombok.Data;

import java.util.List;

/**
 * class info
 *
 * @author xuxueli 2018-05-02 20:02:34
 */
@Data
public class ClassInfo {
    /**
     * dto路径
     */
    private String dtoPackageName;
    /**
     * dto路径
     */
    private String voPackageName;
    /**
     * dto路径
     */
    private String mapperPackageName;
    /**
     * controller路径
     */
    private String controllerPackageName;

    /**
     * 表备注
     */
    private String tableComment;
    /**
     * 去掉前缀的表名  如 ：sys_user_info -> user_info
     */
    private String tableName;
    /**
     * 表名  如 ：sys_user_info
     */
    private String originTableName;
    /**
     * 类名  如 ：UserInfo
     */
    private String className;
    /**
     * 类名  如 ：UserInfoDTO
     */
    private String classNameWithSuffix;

    /**
     * 类备注  如 ：用户信息
     */
    private String classComment;
    private List<FieldInfo> fieldList;

}
