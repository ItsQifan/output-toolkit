package com.zhouchuanxiang.outputtoolkit.codegenerator.service.impl;

import com.zhouchuanxiang.outputtoolkit.codegenerator.entity.ClassInfo;
import com.zhouchuanxiang.outputtoolkit.codegenerator.entity.FieldInfo;
import com.zhouchuanxiang.outputtoolkit.codegenerator.service.IncrementalUpdateService;
import com.zhouchuanxiang.outputtoolkit.codegenerator.util.*;
import com.zhouchuanxiang.outputtoolkit.codegenerator.xmlconfig.GeneratorConfig;
import com.zhouchuanxiang.outputtoolkit.codegenerator.xmlconfig.TableConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author zhouchuanxiang
 * @Description 增量更新服务实现类
 * @Date 2026-02-04
 */
@Service
@Slf4j
public class IncrementalUpdateServiceImpl implements IncrementalUpdateService {

    @Value("${config.xml.absolute.path}")
    private String configXmlAbsolutePath;

    @Value("${ftl.prefix.name}")
    private String ftlPrefixName;

    @Value("${dto.suffix}")
    private String dtoSuffix;

    @Value("${mapper.suffix}")
    private String mapperSuffix;

    @Value("${OEM.author}")
    private String authorName;

    @Autowired
    private XmlGeneratorServiceImpl xmlGeneratorService;

    /**
     * 增量更新的主流程
     */
    @Override
    public String incrementalUpdate(String xmlConfigPath) throws Exception {
        log.info("开始增量更新，配置文件: {}", xmlConfigPath);
        
        // 1. 解析XML配置
        GeneratorConfig generatorConfig = XmlConfigParser.parseFromAbsolutePath(xmlConfigPath);
        if (generatorConfig == null) {
            throw new RuntimeException("XML配置文件解析失败");
        }

        List<TableConfig> tableConfigList = generatorConfig.getTableConfigList();
        StringBuilder resultMsg = new StringBuilder();
        int successCount = 0;
        int failCount = 0;

        // 2. 遍历每个表配置
        for (TableConfig tableConfig : tableConfigList) {
            if (!tableConfig.isGenSwitch()) {
                log.info("表 {} 的生成开关已关闭，跳过", tableConfig.getSql());
                continue;
            }

            try {
                log.info("开始处理表配置...");
                
                // 3. 解析建表语句，获取最新的字段信息
                ClassInfo newClassInfo = xmlGeneratorService.genCodeBySql(tableConfig);
                
                String className = newClassInfo.getClassName();
                String basePath = configXmlAbsolutePath + "\\tmp_useful_code\\";

                // 4. 更新DTO文件
                String dtoFileName = basePath + className + tableConfig.getDtoSuffix() + ".java";
                boolean dtoResult = updateDtoFile(dtoFileName, newClassInfo, tableConfig);
                log.info("DTO文件 {} 更新结果: {}", dtoFileName, dtoResult ? "成功" : "失败或无变更");

//                // 5. 更新VO文件
//                String voFileName = basePath + className + "VO.java";
//                boolean voResult = updateVoFile(voFileName, newClassInfo, tableConfig);
//                log.info("VO文件 {} 更新结果: {}", voFileName, voResult ? "成功" : "失败或无变更");

                // 6. 更新Mapper.xml文件
                String mapperXmlFileName = basePath + className + tableConfig.getMapperSuffix() + ".xml";
                boolean mapperResult = updateMapperXmlFile(mapperXmlFileName, newClassInfo, tableConfig);
                log.info("Mapper.xml文件 {} 更新结果: {}", mapperXmlFileName, mapperResult ? "成功" : "失败或无变更");

                if (dtoResult  || mapperResult) {
                    successCount++;
                    resultMsg.append("✓ ").append(className).append(" 更新成功\n");
                } else {
                    resultMsg.append("- ").append(className).append(" 无变更\n");
                }

            } catch (Exception e) {
                failCount++;
                log.error("处理表失败: {}", tableConfig.getSql(), e);
                resultMsg.append("✗ ").append("处理失败: ").append(e.getMessage()).append("\n");
            }
        }

        String summary = String.format("增量更新完成！成功: %d, 失败: %d\n%s", 
                successCount, failCount, resultMsg.toString());
        log.info(summary);
        return summary;
    }

    /**
     * 更新DTO文件
     */
    @Override
    public boolean updateDtoFile(String filePath, ClassInfo newClassInfo) throws Exception {
        return updateDtoFile(filePath, newClassInfo, null);
    }

    /**
     * 更新DTO文件（带TableConfig参数）
     * 策略：直接重新生成整个DTO文件，避免增量修改导致的格式问题
     */
    private boolean updateDtoFile(String filePath, ClassInfo newClassInfo, TableConfig tableConfig) throws Exception {
        File file = new File(filePath);
        if (!file.exists()) {
            log.warn("DTO文件不存在，跳过更新: {}", filePath);
            return false;
        }

        // 1. 读取现有DTO文件内容
        String content = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
        
        // 2. 解析现有字段
        List<FieldInfo> oldFields = parseDtoFields(content);
        
        // 3. 排除基类字段
        List<String> excludeFields = Arrays.asList("id", "createdAt", "createdBy", "updatedAt", "updatedBy");
        List<FieldInfo> oldFieldsFiltered = FieldDiffUtil.excludeFields(oldFields, excludeFields);
        List<FieldInfo> newFieldsFiltered = FieldDiffUtil.excludeFields(newClassInfo.getFieldList(), excludeFields);
        
        // 4. 比对字段差异
        FieldDiffUtil.FieldDiff diff = FieldDiffUtil.compareFields(oldFieldsFiltered, newFieldsFiltered);
        
        if (!diff.hasChanges()) {
            log.info("DTO文件字段无变更: {}", filePath);
            return false;
        }

        log.info("DTO文件有变更 - 新增: {}, 删除: {}", 
                diff.getAddedFields().size(), diff.getRemovedFields().size());

        // 5. 直接重新生成DTO文件（更安全可靠）
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("classInfo", newClassInfo);
        paramMap.put("dtoSuffix", tableConfig != null ? tableConfig.getDtoSuffix() : dtoSuffix);
        paramMap.put("authorName", tableConfig != null ? tableConfig.getAuthorName() : authorName);
        
        String dtoContent = FreemarkerUtil.processString(ftlPrefixName + "/" + ftlPrefixName + "_dto.ftl", paramMap);
        Files.write(Paths.get(filePath), dtoContent.getBytes(StandardCharsets.UTF_8));
        
        log.info("DTO文件重新生成完成: {}", filePath);
        return true;
    }

    /**
     * 更新VO文件 - 直接重新生成
     */
    @Override
    public boolean updateVoFile(String filePath, ClassInfo newClassInfo) throws Exception {
        return updateVoFile(filePath, newClassInfo, null);
    }

    /**
     * 更新VO文件（带TableConfig参数）
     */
    private boolean updateVoFile(String filePath, ClassInfo newClassInfo, TableConfig tableConfig) throws Exception {
        File file = new File(filePath);
        if (!file.exists()) {
            log.warn("VO文件不存在，跳过更新: {}", filePath);
            return false;
        }

        // VO继承自DTO，只需要重新生成即可
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("classInfo", newClassInfo);
        paramMap.put("dtoSuffix", tableConfig != null ? tableConfig.getDtoSuffix() : dtoSuffix);
        paramMap.put("authorName", tableConfig != null ? tableConfig.getAuthorName() : authorName);
        
        String voContent = FreemarkerUtil.processString(ftlPrefixName + "/" + ftlPrefixName + "_vo.ftl", paramMap);
        Files.write(Paths.get(filePath), voContent.getBytes(StandardCharsets.UTF_8));
        
        log.info("VO文件重新生成完成: {}", filePath);
        return true;
    }

    /**
     * 更新Mapper.xml文件
     */
    @Override
    public boolean updateMapperXmlFile(String filePath, ClassInfo newClassInfo) throws Exception {
        return updateMapperXmlFile(filePath, newClassInfo, null);
    }

    /**
     * 更新Mapper.xml文件（带TableConfig参数）
     */
    private boolean updateMapperXmlFile(String filePath, ClassInfo newClassInfo, TableConfig tableConfig) throws Exception {
        File file = new File(filePath);
        if (!file.exists()) {
            log.warn("Mapper.xml文件不存在，跳过更新: {}", filePath);
            return false;
        }

        // 1. 读取现有Mapper.xml内容
        String content = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
        
        // 2. 解析现有字段
        List<FieldInfo> oldFields = parseMapperXmlFields(content);
        
        // 3. 比对字段差异
        FieldDiffUtil.FieldDiff diff = FieldDiffUtil.compareFields(oldFields, newClassInfo.getFieldList());
        
        if (!diff.hasChanges()) {
            log.info("Mapper.xml文件字段无变更: {}", filePath);
            return false;
        }

        log.info("Mapper.xml有变更 - 新增: {}, 删除: {}", 
                diff.getAddedFields().size(), diff.getRemovedFields().size());

        // 4. 更新resultMap
        content = updateResultMap(content, diff, newClassInfo);
        
        // 5. 更新Base_Column_List
        content = updateBaseColumnList(content, newClassInfo);
        
        // 6. 更新insert语句
        content = updateInsertStatement(content, diff, newClassInfo);
        
        // 7. 更新update语句
        content = updateUpdateStatement(content, diff, newClassInfo);
        
        // 8. 更新pageList的where条件
        content = updatePageListWhere(content, diff, newClassInfo);

        // 9. 写回文件
        Files.write(Paths.get(filePath), content.getBytes(StandardCharsets.UTF_8));
        log.info("Mapper.xml文件更新完成: {}", filePath);
        return true;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 解析DTO文件中的字段
     */
    private List<FieldInfo> parseDtoFields(String content) {
        List<FieldInfo> fields = new ArrayList<>();
        
        // 匹配字段声明：private Type fieldName; 或带注释的字段
        Pattern pattern = Pattern.compile(
            "/\\*\\*[\\s\\S]*?\\*/\\s*private\\s+(\\w+(?:<[^>]+>)?(?:\\[\\])?)\\s+(\\w+);",
            Pattern.MULTILINE
        );
        
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String fieldClass = matcher.group(1);
            String fieldName = matcher.group(2);
            
            FieldInfo field = new FieldInfo();
            field.setFieldName(fieldName);
            field.setFieldClass(fieldClass);
            
            // 尝试提取注释
            String beforeField = content.substring(Math.max(0, matcher.start() - 200), matcher.start());
            Pattern commentPattern = Pattern.compile("/\\*\\*\\s*\\*\\s*([^\\*]+)\\s*\\*/");
            Matcher commentMatcher = commentPattern.matcher(beforeField);
            if (commentMatcher.find()) {
                field.setFieldComment(commentMatcher.group(1).trim());
            }
            
            fields.add(field);
        }
        
        return fields;
    }

    /**
     * 解析Mapper.xml中的字段
     */
    private List<FieldInfo> parseMapperXmlFields(String content) {
        List<FieldInfo> fields = new ArrayList<>();
        
        // 从resultMap中提取字段
        Pattern pattern = Pattern.compile(
            "<result\\s+column=\"([^\"]+)\"[^>]*property=\"([^\"]+)\"[^>]*jdbcType=\"([^\"]+)\"[^>]*/>"
        );
        
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String columnName = matcher.group(1).trim();
            String fieldName = matcher.group(2).trim();
            String jdbcType = matcher.group(3).trim();
            
            FieldInfo field = new FieldInfo();
            field.setColumnName(columnName);
            field.setFieldName(fieldName);
            field.setFieldXmlClass(jdbcType);
            
            fields.add(field);
        }
        
        return fields;
    }

    /**
     * 更新resultMap
     */
    private String updateResultMap(String content, FieldDiffUtil.FieldDiff diff, ClassInfo newClassInfo) {
        // 找到resultMap的起始和结束位置
        Pattern pattern = Pattern.compile(
            "(<resultMap[^>]*>)([\\s\\S]*?)(</resultMap>)",
            Pattern.MULTILINE
        );
        
        Matcher matcher = pattern.matcher(content);
        if (!matcher.find()) {
            log.warn("未找到resultMap节点");
            return content;
        }

        String resultMapStart = matcher.group(1);
        String resultMapEnd = matcher.group(3);
        
        // 计算最大列名和属性名长度（用于格式化对齐）
        int maxColumnLen = newClassInfo.getFieldList().stream()
                .mapToInt(f -> f.getColumnName().length())
                .max()
                .orElse(20);
        int maxPropertyLen = newClassInfo.getFieldList().stream()
                .mapToInt(f -> f.getFieldName().length())
                .max()
                .orElse(20);

        // 重新生成resultMap内容
        StringBuilder newResultMap = new StringBuilder();
        newResultMap.append(resultMapStart).append("\n");
        
        for (FieldInfo field : newClassInfo.getFieldList()) {
            newResultMap.append("        <result column=\"").append(field.getColumnName()).append("\"");
            
            // 添加空格以对齐
            int columnSpaces = maxColumnLen - field.getColumnName().length() + 4;
            for (int i = 0; i < columnSpaces; i++) {
                newResultMap.append(" ");
            }
            
            newResultMap.append("property=\"").append(field.getFieldName()).append("\"");
            
            int propertySpaces = maxPropertyLen - field.getFieldName().length() + 4;
            for (int i = 0; i < propertySpaces; i++) {
                newResultMap.append(" ");
            }
            
            newResultMap.append("jdbcType=\"").append(field.getFieldXmlClass()).append("\" />\n");
        }
        
        newResultMap.append("    ").append(resultMapEnd);
        
        return content.replaceFirst(
            "<resultMap[^>]*>[\\s\\S]*?</resultMap>",
            Matcher.quoteReplacement(newResultMap.toString())
        );
    }

    /**
     * 更新Base_Column_List
     */
    private String updateBaseColumnList(String content, ClassInfo newClassInfo) {
        StringBuilder columnList = new StringBuilder();
        
        for (int i = 0; i < newClassInfo.getFieldList().size(); i++) {
            FieldInfo field = newClassInfo.getFieldList().get(i);
            columnList.append(field.getColumnName());
            if (i < newClassInfo.getFieldList().size() - 1) {
                columnList.append(",");
            }
            columnList.append("\n            ");
        }
        
        String replacement = "<sql id=\"Base_Column_List\">\n        " 
                + columnList.toString().trim() + "\n    </sql>";
        
        return content.replaceFirst(
            "<sql id=\"Base_Column_List\">[\\s\\S]*?</sql>",
            Matcher.quoteReplacement(replacement)
        );
    }

    /**
     * 更新insert语句
     */
    private String updateInsertStatement(String content, FieldDiffUtil.FieldDiff diff, ClassInfo newClassInfo) {
        // 找到insert标签
        Pattern pattern = Pattern.compile(
            "(<insert[^>]*>)([\\s\\S]*?)(</insert>)",
            Pattern.MULTILINE
        );
        
        Matcher matcher = pattern.matcher(content);
        if (!matcher.find()) {
            log.warn("未找到insert节点");
            return content;
        }

        String insertStart = matcher.group(1);
        String insertEnd = matcher.group(3);
        
        // 重新生成insert语句
        StringBuilder newInsert = new StringBuilder();
        newInsert.append(insertStart).append("\n");
        newInsert.append("        INSERT INTO ").append(newClassInfo.getOriginTableName()).append("\n");
        newInsert.append("        <trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">\n");
        
        // 字段列表
        for (FieldInfo field : newClassInfo.getFieldList()) {
            if ("id".equals(field.getColumnName())) {
                continue; // 跳过id字段
            }
            
            newInsert.append("            <if test=\"").append(field.getFieldName()).append("!= null");
            if ("String".equals(field.getFieldClass())) {
                newInsert.append(" and  ").append(field.getFieldName()).append(" !=''");
            }
            newInsert.append("\">\n");
            newInsert.append("                ").append(field.getColumnName()).append(",\n");
            newInsert.append("            </if>\n");
        }
        
        newInsert.append("        </trim>\n");
        newInsert.append("        <trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\">\n");
        
        // 值列表
        for (FieldInfo field : newClassInfo.getFieldList()) {
            if ("id".equals(field.getColumnName())) {
                continue;
            }
            
            newInsert.append("            <if test=\"").append(field.getFieldName()).append("!=null");
            if ("String".equals(field.getFieldClass())) {
                newInsert.append(" and  ").append(field.getFieldName()).append(" !=''");
            }
            newInsert.append("\">\n");
            newInsert.append("                #{").append(field.getFieldName()).append("},\n");
            newInsert.append("            </if>\n");
        }
        
        newInsert.append("        </trim>\n");
        newInsert.append("    ").append(insertEnd);
        
        return content.replaceFirst(
            "<insert[^>]*>[\\s\\S]*?</insert>",
            Matcher.quoteReplacement(newInsert.toString())
        );
    }

    /**
     * 更新update语句
     */
    private String updateUpdateStatement(String content, FieldDiffUtil.FieldDiff diff, ClassInfo newClassInfo) {
        // 找到update标签
        Pattern pattern = Pattern.compile(
            "(<update[^>]*>)([\\s\\S]*?)(</update>)",
            Pattern.MULTILINE
        );
        
        Matcher matcher = pattern.matcher(content);
        if (!matcher.find()) {
            log.warn("未找到update节点");
            return content;
        }

        String updateStart = matcher.group(1);
        String updateEnd = matcher.group(3);
        
        // 重新生成update语句
        StringBuilder newUpdate = new StringBuilder();
        newUpdate.append(updateStart).append("\n");
        newUpdate.append("        UPDATE ").append(newClassInfo.getOriginTableName()).append("\n");
        newUpdate.append("        <set>\n");
        
        for (FieldInfo field : newClassInfo.getFieldList()) {
            if ("id".equals(field.getColumnName()) || 
                "AddTime".equals(field.getColumnName()) || 
                "UpdateTime".equals(field.getColumnName())) {
                continue;
            }
            
            newUpdate.append("            <if test=\" ").append(field.getFieldName()).append("!= null");
            if ("String".equals(field.getFieldClass())) {
                newUpdate.append(" and  ").append(field.getFieldName()).append(" !=''");
            }
            newUpdate.append("\">\n");
            newUpdate.append("                ").append(field.getColumnName()).append(" = #{");
            newUpdate.append(field.getFieldName()).append("},\n");
            newUpdate.append("            </if>\n");
        }
        
        newUpdate.append("        </set>\n");
        newUpdate.append("        WHERE id = #{id}\n");
        newUpdate.append("    ").append(updateEnd);
        
        return content.replaceFirst(
            "<update[^>]*>[\\s\\S]*?</update>",
            Matcher.quoteReplacement(newUpdate.toString())
        );
    }

    /**
     * 更新pageList的where条件
     */
    private String updatePageListWhere(String content, FieldDiffUtil.FieldDiff diff, ClassInfo newClassInfo) {
        // 找到pageList的where部分
        Pattern pattern = Pattern.compile(
            "(<select[^>]*id=\"pageList\"[^>]*>)([\\s\\S]*?)(</select>)",
            Pattern.MULTILINE
        );
        
        Matcher matcher = pattern.matcher(content);
        if (!matcher.find()) {
            log.warn("未找到pageList的select节点");
            return content;
        }

        String selectStart = matcher.group(1);
        String selectEnd = matcher.group(3);
        
        // 重新生成select语句
        StringBuilder newSelect = new StringBuilder();
        newSelect.append(selectStart).append("\n");
        newSelect.append("        SELECT\n");
        newSelect.append("        <include refid=\"Base_Column_List\" />\n");
        newSelect.append("        FROM ").append(newClassInfo.getOriginTableName()).append("\n");
        newSelect.append("        <where>\n");
        
        for (FieldInfo field : newClassInfo.getFieldList()) {
            if ("id".equals(field.getColumnName())) {
                continue;
            }
            
            newSelect.append("            <if test=\"").append(field.getFieldName()).append("!= null");
            if ("String".equals(field.getFieldClass())) {
                newSelect.append(" and  ").append(field.getFieldName()).append(" !=''");
            }
            newSelect.append("\">\n");
            newSelect.append("                and ").append(field.getColumnName()).append(" = #{");
            newSelect.append(field.getFieldName()).append(",jdbcType=").append(field.getFieldXmlClass());
            newSelect.append("}\n");
            newSelect.append("            </if>\n");
        }
        
        newSelect.append("        </where>\n");
        newSelect.append("        order by CREATED_AT desc\n");
        newSelect.append("    ").append(selectEnd);
        
        return content.replaceFirst(
            "<select[^>]*id=\"pageList\"[^>]*>[\\s\\S]*?</select>",
            Matcher.quoteReplacement(newSelect.toString())
        );
    }
}
