package com.zhouchuanxiang.outputtoolkit.codegenerator.util;

import java.util.HashMap;

/**
 * @author lvyanpu
 */
public final class MysqlJavaTypeUtil {
    public static final HashMap<String, String> mysqlJavaTypeMap = new HashMap<String, String>();
    public static final HashMap<String, String> mysqlSwaggerTypeMap = new HashMap<String, String>();
    //mapper.xml内的字段类型
    public static final HashMap<String, String> mysqlXmlTypeMap = new HashMap<String, String>();

    static {
        mysqlJavaTypeMap.put("bigint", "Long");
        mysqlJavaTypeMap.put("int", "Integer");
        mysqlJavaTypeMap.put("tinyint", "Integer");
        mysqlJavaTypeMap.put("smallint", "Integer");
        mysqlJavaTypeMap.put("mediumint", "Integer");
        mysqlJavaTypeMap.put("integer", "Integer");
        //小数
        mysqlJavaTypeMap.put("float", "Float");
        mysqlJavaTypeMap.put("double", "Double");
        mysqlJavaTypeMap.put("decimal", "Double");
        //bool
        mysqlJavaTypeMap.put("bit", "Boolean");
        //字符串
        mysqlJavaTypeMap.put("char", "String");
        mysqlJavaTypeMap.put("varchar", "String");
        mysqlJavaTypeMap.put("tinytext", "String");
        mysqlJavaTypeMap.put("text", "String");
        mysqlJavaTypeMap.put("mediumtext", "String");
        mysqlJavaTypeMap.put("longtext", "String");
        //日期
        mysqlJavaTypeMap.put("date", "Date");
        mysqlJavaTypeMap.put("datetime", "Date");
        mysqlJavaTypeMap.put("timestamp", "Date");


        mysqlSwaggerTypeMap.put("bigint", "integer");
        mysqlSwaggerTypeMap.put("int", "integer");
        mysqlSwaggerTypeMap.put("tinyint", "integer");
        mysqlSwaggerTypeMap.put("smallint", "integer");
        mysqlSwaggerTypeMap.put("mediumint", "integer");
        mysqlSwaggerTypeMap.put("integer", "integer");
        mysqlSwaggerTypeMap.put("boolean", "boolean");
        mysqlSwaggerTypeMap.put("float", "number");
        mysqlSwaggerTypeMap.put("double", "number");
        mysqlSwaggerTypeMap.put("decimal", "Double");


// 数值类型
        mysqlXmlTypeMap.put("tinyint", "TINYINT");
        mysqlXmlTypeMap.put("smallint", "SMALLINT");
        mysqlXmlTypeMap.put("mediumint", "INTEGER");
        mysqlXmlTypeMap.put("int", "INTEGER");
        mysqlXmlTypeMap.put("integer", "INTEGER");
        mysqlXmlTypeMap.put("bigint", "BIGINT");
        mysqlXmlTypeMap.put("float", "FLOAT");
        mysqlXmlTypeMap.put("double", "DOUBLE");
        mysqlXmlTypeMap.put("decimal", "DECIMAL");
        mysqlXmlTypeMap.put("numeric", "NUMERIC");

// 日期时间类型
        mysqlXmlTypeMap.put("date", "DATE");
        mysqlXmlTypeMap.put("time", "TIME");
        mysqlXmlTypeMap.put("year", "CHAR");
        mysqlXmlTypeMap.put("datetime", "TIMESTAMP");
        mysqlXmlTypeMap.put("timestamp", "TIMESTAMP");

// 字符串类型
        mysqlXmlTypeMap.put("char", "CHAR");
        mysqlXmlTypeMap.put("varchar", "VARCHAR");
        mysqlXmlTypeMap.put("tinytext", "VARCHAR");
        mysqlXmlTypeMap.put("text", "VARCHAR");
        mysqlXmlTypeMap.put("mediumtext", "VARCHAR");
        mysqlXmlTypeMap.put("longtext", "VARCHAR");

// 二进制类型
        mysqlXmlTypeMap.put("binary", "BINARY");
        mysqlXmlTypeMap.put("varbinary", "VARBINARY");
        mysqlXmlTypeMap.put("tinyblob", "BLOB");
        mysqlXmlTypeMap.put("blob", "BLOB");
        mysqlXmlTypeMap.put("mediumblob", "BLOB");
        mysqlXmlTypeMap.put("longblob", "BLOB");

// 其他类型
        mysqlXmlTypeMap.put("bit", "BIT");
        mysqlXmlTypeMap.put("enum", "CHAR");
        mysqlXmlTypeMap.put("set", "CHAR");
        mysqlXmlTypeMap.put("json", "VARCHAR");


    }

    public static HashMap<String, String> getMysqlJavaTypeMap() {
        return mysqlJavaTypeMap;
    }

    public static HashMap<String, String> getMysqlSwaggerTypeMap() {
        return mysqlSwaggerTypeMap;
    }

    public static HashMap<String, String> getMysqlXmlTypeMap() {
        return mysqlXmlTypeMap;
    }
}
