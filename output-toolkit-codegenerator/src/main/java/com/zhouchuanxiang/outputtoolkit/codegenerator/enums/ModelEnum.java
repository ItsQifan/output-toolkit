package com.zhouchuanxiang.outputtoolkit.codegenerator.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author zhouchuanxiang
 * @Description 模式
 * @Date 14:56 2025/8/7
 * @Param
 * @return
 **/
@Getter
public enum ModelEnum {
    /**
     * 建表语句
     */
    SQL("sql", "建表语句"),

    ;
    /**
     * 编码
     */
    private String code;
    /**
     * 描述
     */
    private String desc;

    ModelEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code找enum
     *
     * @param code
     * @return
     */
    public static ModelEnum getByCode(String code) {
        if (StringUtils.isNotBlank(code)) {
            for (ModelEnum modelEnum : ModelEnum.values()) {
                if (code.equals(modelEnum.getCode())) {
                    return modelEnum;
                }
            }
        }
        return null;
    }

    /**
     * 根据code找enum的desc
     *
     * @param code
     * @return
     */
    public static String getDescByCode(String code) {
        if (StringUtils.isNotBlank(code)) {
            for (ModelEnum modelEnum : ModelEnum.values()) {
                if (code.equals(modelEnum.getCode())) {
                    return modelEnum.getDesc();
                }
            }
        }
        return StringUtils.EMPTY;
    }


}
