package com.zhouchuanxiang.outputtoolkit.codegenerator.util;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.zhouchuanxiang.outputtoolkit.codegenerator.xmlconfig.GeneratorConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * @Author zhouchuanxiang
 * @Description xml 解析类,解析config xml文件
 * @Date 14:46 2025/9/26
 * @Param
 * @return
 **/
@Component
@Slf4j
public class XmlConfigParser {

    /**
     * @Author zhouchuanxiang
     * @Description  解析xml
     * @Date 15:23 2025/9/26
     * @Param [filePath]
     * @return
     **/
    public static GeneratorConfig parseConfig(String filePath) {
        try {
            XmlMapper xmlMapper = new XmlMapper();
            //使用Spring的ClassPathResource --  读取本项目resources下的文件
//            ClassPathResource resource = new ClassPathResource(filePath);
//            if (!resource.exists()) {
//                throw new RuntimeException("文件在类路径中未找到: " + filePath);
//            }
//            GeneratorConfig config = xmlMapper.readValue(resource.getInputStream(), GeneratorConfig.class);

            //2 - 读取本地绝对路径下的文件
            File file = new File(filePath);
            GeneratorConfig config = xmlMapper.readValue(file, GeneratorConfig.class);

            return config;
        } catch (IOException e) {
            log.error("XmlConfigParser Failed to parse XML configuration",e);
            throw new RuntimeException("Failed to parse XML configuration", e);
        }
    }


    /**
     * 读取D盘绝对路径下的XML文件
     * @param absolutePath 绝对路径，如 "D:/config/app-config.xml"
     */
    public static GeneratorConfig parseFromAbsolutePath(String absolutePath) {
        try {
            XmlMapper xmlMapper = new XmlMapper();
            File file = new File(absolutePath);
            // 检查文件是否存在
            if (!file.exists()) {
                throw new RuntimeException("文件不存在: " + absolutePath);
            }
            // 检查是否可读
            if (!file.canRead()) {
                throw new RuntimeException("文件不可读: " + absolutePath);
            }
            GeneratorConfig config = xmlMapper.readValue(file, GeneratorConfig.class);
            return config;
        } catch (Exception e) {
            throw new RuntimeException("解析XML文件失败: " + absolutePath, e);
        }
    }

    public static void main(String[] args) {
//        String template = ResourceUtils.CLASSPATH_URL_PREFIX  + "generator-config.xml";
//        String template = "generator-config.xml";
        String template = "D:\\【甜橙金融】\\test代码生成\\generatorConfig.xml";
//        log.info("XmlConfigParser,{}", JSON.toJSONString(parseConfig(template)));
        log.info("XmlConfigParser,{}", JSON.toJSONString(parseFromAbsolutePath(template)));
    }
}
