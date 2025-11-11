package com.zhouchuanxiang.outputtoolkit.codegenerator.tool;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zhouchuanxiang.outputtoolkit.codegenerator.util.FreemarkerUtil;
import com.zhouchuanxiang.outputtoolkit.codegenerator.util.MapUtil;
import freemarker.template.TemplateException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author zhouchuanxiang
 * @Description 模板工具类，读取模板，写入模板等
 * @Date 15:07 2025/9/29
 * @Param
 * @return
 **/
@Component
@Slf4j
public class TemplateTool {
    /**
     *
     */
    @Value("${template.json.file}")
    private String templateJsonFile;
    /**
     * xml配置文件的文件夹
     */
    @Value("${config.xml.absolute.path}")
    private String configXmlAbsolutePath;

    public JSONArray templateArray = null;

    public JSONArray getTemplateArray() {
        return templateArray;
    }


    @PostConstruct
    public void init() {
        try {
            templateArray = getAllTemplateConfigArray();
        } catch (IOException e) {
            log.error("从项目中的JSON文件读取模板 失败！", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 从项目中的JSON文件读取模板
     * 返回所有的模板
     */
    public JSONArray getAllTemplateConfigArray() throws IOException {
        String templateConfig = null;
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(templateJsonFile);
        templateConfig = new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining(System.lineSeparator()));
        inputStream.close();
//        log.info(JSON.toJSONString(templateConfig));
        return JSONArray.parseArray(templateConfig);
    }


    /**
     * 根据配置的Template模板进行遍历解析，得到生成好的String
     * 一次性生成所有
     */
    public Map<String, String> getAllResultByParams(Map<String, Object> params) throws IOException, TemplateException {
        Map<String, String> result = new HashMap<>(32);
        result.put("tableName", MapUtil.getString(params, "tableName"));
        JSONArray parentTemplates = getAllTemplateConfigArray();
        for (int i = 0; i < parentTemplates.size(); i++) {
            JSONObject parentTemplateObj = parentTemplates.getJSONObject(i);
            for (int x = 0; x < parentTemplateObj.getJSONArray("templates").size(); x++) {
                JSONObject childTemplate = parentTemplateObj.getJSONArray("templates").getJSONObject(x);
                result.put( parentTemplateObj.getString("group") + "@" +childTemplate.getString("name"),
                        FreemarkerUtil.processString(parentTemplateObj.getString("group") + "/" + childTemplate.getString("name") + ".ftl", params));
            }
        }
        return result;
    }


    /**
     * @return
     * @Author zhouchuanxiang
     * @Description 将内容写入到指定文件
     * @Date 15:29 2025/9/29
     * @Param [content, fileName]
     **/
    public boolean writeContentToFile(String content, String fileName) {
        log.info("writeContentToFile start,fileName:{}", fileName);

        File file = new File(fileName);
        // 获取父目录
        File parentDir = file.getParentFile();

        // 如果父目录不存在，则创建目录
        if (parentDir != null && !parentDir.exists()) {
            boolean dirsCreated = parentDir.mkdirs();
            if (!dirsCreated) {
                log.error("Failed to create directories: {}", parentDir.getAbsolutePath());
                return false;
            }
            log.info("Created directories: {}", parentDir.getAbsolutePath());
        }

        // 生成输出
        // 使用try-with-resources语句，自动关闭资源
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(content);
        } catch (IOException e) {
            log.error("writeContentToFile error", e);
            return false;
        }

        return true;
    }




}
