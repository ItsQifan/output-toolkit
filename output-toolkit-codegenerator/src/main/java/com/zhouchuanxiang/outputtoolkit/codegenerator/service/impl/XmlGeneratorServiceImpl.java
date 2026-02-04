package com.zhouchuanxiang.outputtoolkit.codegenerator.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.zhouchuanxiang.outputtoolkit.codegenerator.entity.ClassInfo;
import com.zhouchuanxiang.outputtoolkit.codegenerator.entity.FieldInfo;
import com.zhouchuanxiang.outputtoolkit.codegenerator.entity.NonCaseString;
import com.zhouchuanxiang.outputtoolkit.codegenerator.enums.ModelEnum;
import com.zhouchuanxiang.outputtoolkit.codegenerator.service.XmlGeneratorService;
import com.zhouchuanxiang.outputtoolkit.codegenerator.tool.TemplateTool;
import com.zhouchuanxiang.outputtoolkit.codegenerator.util.*;
import com.zhouchuanxiang.outputtoolkit.codegenerator.xmlconfig.GeneratorConfig;
import com.zhouchuanxiang.outputtoolkit.codegenerator.xmlconfig.TableConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Author zhouchuanxiang
 * @Description xml生成代码实现类
 * @Date 15:30 2025/9/26
 * @Param
 * @return
 **/
@Component
@Slf4j
public class XmlGeneratorServiceImpl implements XmlGeneratorService {


    @Value("${config.xml.absolute.path}")
    private String configXmlAbsolutePath;

    /**
     * 建表语句文件根路径
     */
    @Value("${sql.absolute.path}")
    private String sqlAbsolutePath;
    /**
     * dto 前缀
     */
    @Value("${dto.suffix}")
    private String dtoSuffix;
    /**
     * mapper 前缀
     */
    @Value("${mapper.suffix}")
    private String mapperSuffix;
    /**
     * manager 前缀
     */
    @Value("${manager.suffix}")
    private String managerSuffix;
    /**
     * dto 包路径
     */
    @Value("${dto.packageName}")
    private String dtoPackageName;
    /**
     * Vo 包路径
     */
    @Value("${vo.packageName}")
    private String voPackageName;
    /**
     * mapper 包路径
     */
    @Value("${mapper.packageName}")
    private String mapperPackageName;
    /**
     * manager 包路径
     */
    @Value("${manager.packageName}")
    private String managerPackageName;
    /**
     * controller 包路径
     */
    @Value("${controller.packageName}")
    private String controllerPackageName;
    /**
     * ftl模板文件的名字前缀
     */
    @Value("${ftl.prefix.name}")
    private String ftlPrefixName;

    @Autowired
    private TemplateTool templateTool;

    /**
     * @return
     * @Author zhouchuanxiang
     * @Description 执行生成
     * @Date 15:31 2025/9/26
     * @Param []
     **/
    @Override
    public List<ClassInfo> doGenerate(Map<String, String> paramMap) throws Exception {

        String generatorConfigPath = paramMap.get("generatorConfigPath");
        //读取xml配置
        GeneratorConfig generatorConfig = XmlConfigParser.parseFromAbsolutePath(generatorConfigPath);
        if (generatorConfig == null) {
            throw new RuntimeException("xml配置文件解析为bean失败");
        }
        List<TableConfig> tableConfigList = generatorConfig.getTableConfigList();

        List<ClassInfo> classInfoList = new ArrayList<>();

        for (TableConfig tableConfig : tableConfigList) {
            //开关关闭，跳过
            if (!tableConfig.isGenSwitch()) {
                continue;
            }

            if (StringUtils.isBlank(tableConfig.getModel())) {
                throw new CodeGenerateException("model can not be empty. model不能为空。");
            }

            ClassInfo classInfo = new ClassInfo();

            String model = Optional.ofNullable(tableConfig.getModel()).orElse(ModelEnum.SQL.getCode());
            //建表语句方式
            switch (model) {
                case "sql":
                    classInfo = genCodeBySql(tableConfig);
                    break;
                case "json":
                    //todo
                    break;
                default:
                    throw new CodeGenerateException("model can not be empty. model不能为空。");
            }

            //2.Set the params 设置表格参数
            Map<String, Object> paramInfoMap = BeanUtil.beanToMap(tableConfig);
            paramInfoMap.put("classInfo", classInfo);

//        paramInfo.getOptions().put("tableName", classInfo == null ? System.currentTimeMillis() : classInfo.getTableName());

            //log the generated table and filed size记录解析了什么表，有多少个字段
            //log.info("generated table :{} , size :{}",classInfo.getTableName(),(classInfo.getFieldList() == null ? "" : classInfo.getFieldList().size()));

            //3.generate the code by freemarker templates with parameters . Freemarker根据参数和模板生成代码
            Map<String, String> result = templateTool.getAllResultByParams(paramInfoMap);
            log.info("xml result: {}", result);
            log.info("table:{} - time:{} ", MapUtil.getString(result, "tableName"), new Date());

            //todo 将生成的代码写入到文件中
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> stringStringEntry : result.entrySet()) {
                if (!stringStringEntry.getKey().contains(ftlPrefixName)) {
                    String fileName = configXmlAbsolutePath + "\\tmp_code\\" + stringStringEntry.getKey() + ".java";
                    templateTool.writeContentToFile(stringStringEntry.getValue(), fileName);
                }
                //输出到文件，重命名
                if (stringStringEntry.getKey().contains(ftlPrefixName + "_controller")) {
                    String fileName = configXmlAbsolutePath + "\\tmp_useful_code\\" + classInfo.getClassName() + "Controller.java";
                    templateTool.writeContentToFile(stringStringEntry.getValue(), fileName);
                } else if (stringStringEntry.getKey().contains(ftlPrefixName + "_mapper")) {
                    String fileName = configXmlAbsolutePath + "\\tmp_useful_code\\" + classInfo.getClassName() + tableConfig.getMapperSuffix() + ".java";
                    templateTool.writeContentToFile(stringStringEntry.getValue(), fileName);
                } else if (stringStringEntry.getKey().contains(ftlPrefixName + "_dto")) {
                    String fileName = configXmlAbsolutePath + "\\tmp_useful_code\\" + classInfo.getClassName()+tableConfig.getDtoSuffix() + ".java";
                    templateTool.writeContentToFile(stringStringEntry.getValue(), fileName);
                } else if (stringStringEntry.getKey().contains(ftlPrefixName + "_vo")) {
                    String fileName = configXmlAbsolutePath + "\\tmp_useful_code\\" + classInfo.getClassName() + "VO.java";
                    templateTool.writeContentToFile(stringStringEntry.getValue(), fileName);
                } else if (stringStringEntry.getKey().contains(ftlPrefixName + "_mybatis_xml")) {
                    String fileName = configXmlAbsolutePath + "\\tmp_useful_code\\" + classInfo.getClassName() + tableConfig.getMapperSuffix() + ".xml";
                    templateTool.writeContentToFile(stringStringEntry.getValue(), fileName);
                }else if (stringStringEntry.getKey().contains(ftlPrefixName + "_manager") && !stringStringEntry.getKey().contains("impl")) {
                    String fileName = configXmlAbsolutePath + "\\tmp_useful_code\\" + classInfo.getClassName() + tableConfig.getManagerSuffix() + ".java";
                    templateTool.writeContentToFile(stringStringEntry.getValue(), fileName);
                }else if (stringStringEntry.getKey().contains(ftlPrefixName + "_manager_impl")) {
                    String fileName = configXmlAbsolutePath + "\\tmp_useful_code\\" + classInfo.getClassName() + tableConfig.getManagerSuffix() + "Impl.java";
                    templateTool.writeContentToFile(stringStringEntry.getValue(), fileName);
                }
            }
        }


        return classInfoList;
    }

    /**
     * @return
     * @Author zhouchuanxiang
     * @Description 生成xml配置文件，支持批量处理多个建表语句
     * @Date 14:30 2025/9/29
     * @Param []
     **/
    @Override
    public String generateXml() {
        List<TableConfig> tableConfigs = new ArrayList<>();
        String content = null;
        try {
            content = new String(Files.readAllBytes(Paths.get(sqlAbsolutePath)), StandardCharsets.UTF_8);
            // 统一转换为小写，便于正则匹配
            content = content.replaceAll("(?i)CREATE TABLE", "create table");
            // 移除SQL注释（-- 和 # 开头的注释）
            content = content.replaceAll("(?m)^\\s*--.*$", "");
            content = content.replaceAll("(?m)^\\s*#.*$", "");
            // 移除多余的空行
            content = content.replaceAll("(?m)^\\s*$[\r\n]+", "");
        } catch (IOException e) {
            log.error("读取SQL文件失败: {}", sqlAbsolutePath, e);
            throw new RuntimeException("读取SQL文件失败: " + sqlAbsolutePath, e);
        }

        // 优化后的正则表达式：匹配CREATE TABLE语句
        // 支持多种格式：
        //   - CREATE TABLE `table_name` (...) COMMENT = '...';
        //   - CREATE TABLE `table_name` (...);
        //   - CREATE TABLE `table_name` (...) ENGINE=InnoDB COMMENT='...';
        // 注意：使用非贪婪匹配，确保能正确分割多个CREATE TABLE语句
        // 匹配规则：create table + 表名 + (字段定义) + 可选的表选项 + ;
        // 关键：匹配到分号为止，支持 COMMENT = '...' 等表选项
        Pattern pattern = Pattern.compile(
            "create\\s+table\\s+(?:if\\s+not\\s+exists\\s+)?[`'\"]([^`'\"]+)[`'\"][\\s\\S]*?\\)[^;]*;",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL
        );
        Matcher matcher = pattern.matcher(content);
        
        log.debug("开始匹配建表语句，SQL内容长度: {}", content.length());
        if (log.isDebugEnabled()) {
            log.debug("SQL内容预览（前500字符）: {}", content.substring(0, Math.min(500, content.length())));
        }

        int tableCount = 0;
        while (matcher.find()) {
            tableCount++;
            String createTableSql = matcher.group(0).trim();
            log.debug("匹配到第{}个建表语句，长度: {}，预览: {}", 
                tableCount, createTableSql.length(), 
                createTableSql.substring(0, Math.min(100, createTableSql.length())) + "...");
            
            try {
                TableConfig tableConfig = new TableConfig();
                tableConfig.setSql(createTableSql);
                tableConfig.setDtoPackageName(dtoPackageName);
                tableConfig.setVoPackageName(voPackageName);
                tableConfig.setMapperPackageName(mapperPackageName);
                tableConfig.setManagerPackageName(managerPackageName);
                tableConfig.setControllerPackageName(controllerPackageName);
                tableConfig.setDtoSuffix(dtoSuffix);
                tableConfig.setMapperSuffix(mapperSuffix);
                tableConfig.setManagerSuffix(managerSuffix);

                // 提取表名
                String tableName = extractTableName(createTableSql);
                log.debug("提取到表名: {}", tableName);
                
                // 智能提取表名前缀（支持多种情况）
                String prefix = extractTablePrefix(tableName);
                tableConfig.setSqlIgnorePrefix(prefix);
                
                log.info("【批量生成】第{}个表: {} (前缀: {})", tableCount, tableName, prefix);
                tableConfigs.add(tableConfig);
            } catch (Exception e) {
                log.error("【批量生成】解析第{}个建表语句失败，跳过该表", tableCount, e);
                // 继续处理下一个表，不中断整个流程
            }
        }

        if (tableConfigs.isEmpty()) {
            log.warn("【批量生成】未找到任何建表语句，请检查SQL文件格式");
            throw new RuntimeException("未找到任何建表语句，请检查SQL文件格式是否正确");
        }

        log.info("【批量生成】共解析到 {} 个建表语句，准备生成XML配置", tableConfigs.size());

        Map<String, Object> paramInfoMap = new HashMap<>();
        paramInfoMap.put("tableConfigs", tableConfigs);

        String xmlContent = null;
        try {
            xmlContent = FreemarkerUtil.processString("generator-config-xml.ftl", paramInfoMap);
        } catch (Exception e) {
            log.error("生成xml配置文件异常", e);
            throw new RuntimeException("生成xml配置文件异常", e);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        String filePath = configXmlAbsolutePath + "\\xml\\" + "generatorConfig" + sdf.format(new Date()) + ".xml";
        log.info("【批量生成】XML配置文件路径: {}", filePath);

        boolean b = templateTool.writeContentToFile(xmlContent, filePath);
        if (!b) {
            throw new RuntimeException("写入XML配置文件失败: " + filePath);
        }
        
        log.info("【批量生成】XML配置文件生成成功，包含 {} 个表的配置", tableConfigs.size());
        return filePath;
    }

    /**
     * 提取表名
     */
    private String extractTableName(String createTableSql) {
        // 匹配 CREATE TABLE `table_name` 或 CREATE TABLE 'table_name' 或 CREATE TABLE "table_name"
        Pattern tableNamePattern = Pattern.compile(
            "create\\s+table\\s+(?:if\\s+not\\s+exists\\s+)?[`'\"]([^`'\"]+)[`'\"]",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = tableNamePattern.matcher(createTableSql);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        throw new RuntimeException("无法从建表语句中提取表名: " + createTableSql.substring(0, Math.min(100, createTableSql.length())));
    }

    /**
     * 智能提取表名前缀
     * 支持多种情况：
     * - t_p_user_info -> t_p_
     * - r_cb_cm_base -> r_cb_
     * - user_info -> "" (无前缀)
     * - t_user -> t_ (只有一个下划线)
     */
    private String extractTablePrefix(String tableName) {
        if (StringUtils.isBlank(tableName)) {
            return "";
        }
        
        // 查找第二个下划线的位置
        int firstUnderscoreIndex = tableName.indexOf("_");
        if (firstUnderscoreIndex == -1) {
            // 没有下划线，返回空前缀
            return "";
        }
        
        int secondUnderscoreIndex = tableName.indexOf("_", firstUnderscoreIndex + 1);
        if (secondUnderscoreIndex == -1) {
            // 只有一个下划线，返回到第一个下划线+1
            return tableName.substring(0, firstUnderscoreIndex + 1);
        }
        
        // 有两个或更多下划线，返回到第二个下划线+1
        return tableName.substring(0, secondUnderscoreIndex + 1);
    }

    /**
     * @return
     * @Author zhouchuanxiang
     * @Description 根据建表语句 生成代码
     * @Date 17:30 2025/9/28
     * @Param [tableConfig, classInfoList]
     **/
    private ClassInfo genCodeBySql(TableConfig tableConfig) {

        //获取
        NonCaseString tableSql = this.getTableSql(tableConfig);

        // table Name 获取原始表名
        String originTableName = this.getOriginTableName(tableSql);

        //ignore prefix
        String tableName = originTableName;
        if (StringUtils.isNotBlank(tableName) && StringUtils.isNotBlank(tableConfig.getSqlIgnorePrefix())) {
            tableName = tableName.replaceAll(tableConfig.getSqlIgnorePrefix(), "");
        }

        // class Name
        String className = StringUtilsPlus.upperCaseFirst(StringUtilsPlus.underlineToCamelCase(tableName));
        if (className.contains("_")) {
            className = className.replaceAll("_", "");
        }

        tableSql = tableSql.replaceAll("COMMENT", " comment ");

        // class Comment
        String classComment = this.getClassComment(tableSql, className, tableName);


        // field List
        List<FieldInfo> fieldList = new ArrayList<FieldInfo>();


        // 正常( ) 内的一定是字段相关的定义。
        String fieldListTmp = tableSql.substring(tableSql.indexOf("(") + 1, tableSql.lastIndexOf(")")).get();

        // 匹配 comment，替换备注里的小逗号, 防止不小心被当成切割符号切割
        fieldListTmp = replaceCommentCommas(fieldListTmp);

        String commentPattenStr1 = "comment `(.*?)\\`";
        Matcher matcher1 = Pattern.compile(commentPattenStr1).matcher(fieldListTmp);
        while (matcher1.find()) {

            String commentTmp = matcher1.group();
            //2018-9-27 zhengk 不替换，只处理，支持COMMENT评论里面多种注释
            //commentTmp = commentTmp.replaceAll("\\ comment `|\\`", " ");      // "\\{|\\}"

            if (commentTmp.contains(",")) {
                String commentTmpFinal = commentTmp.replaceAll(",", "，");
                fieldListTmp = fieldListTmp.replace(matcher1.group(), commentTmpFinal);
            }
        }
        //2018-10-18 zhengkai 新增支持double(10, 2)等类型中有英文逗号的特殊情况
        String commentPattenStr2 = "\\`(.*?)\\`";
        Matcher matcher2 = Pattern.compile(commentPattenStr2).matcher(fieldListTmp);
        while (matcher2.find()) {
            String commentTmp2 = matcher2.group();
            if (commentTmp2.contains(",")) {
                String commentTmpFinal = commentTmp2.replaceAll(",", "，").replaceAll("\\(", "（").replaceAll("\\)", "）");
                fieldListTmp = fieldListTmp.replace(matcher2.group(), commentTmpFinal);
            }
        }
        //2018-10-18 zhengkai 新增支持double(10, 2)等类型中有英文逗号的特殊情况
        String commentPattenStr3 = "\\((.*?)\\)";
        Matcher matcher3 = Pattern.compile(commentPattenStr3).matcher(fieldListTmp);
        while (matcher3.find()) {
            String commentTmp3 = matcher3.group();
            if (commentTmp3.contains(",")) {
                String commentTmpFinal = commentTmp3.replaceAll(",", "，");
                fieldListTmp = fieldListTmp.replace(matcher3.group(), commentTmpFinal);
            }
        }
        String[] fieldLineList = fieldListTmp.split(",");
        if (fieldLineList.length > 0) {
            int i = 0;
            //i为了解决primary key关键字出现的地方，出现在前3行，一般和id有关
            for (String columnLine0 : fieldLineList) {
                NonCaseString columnLine = NonCaseString.of(columnLine0);
                i++;
                columnLine = columnLine.replaceAll("\n", "").replaceAll("\t", "").trim();
                // `userid` int(11) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                // 2018-9-18 zhengk 修改为contains，提升匹配率和匹配不按照规矩出牌的语句
                // 2018-11-8 zhengkai 修复tornadoorz反馈的KEY FK_permission_id (permission_id),KEY FK_role_id (role_id)情况
                // 2019-2-22 zhengkai 要在条件中使用复杂的表达式
                // 2019-4-29 zhengkai 优化对普通和特殊storage关键字的判断（感谢@AhHeadFloating的反馈 ）
                // 2020-10-20 zhengkai 优化对fulltext/index关键字的处理（感谢@WEGFan的反馈）
                // 2023-8-27 L&J 改用工具方法判断, 且修改变量名(非特殊标识), 方法抽取
                boolean notSpecialFlag = this.isNotSpecialColumnLine(columnLine, i);

                if (notSpecialFlag) {
                    //如果是oracle的number(x,x)，可能出现最后分割残留的,x)，这里做排除处理
                    if (columnLine.length() < 5) {
                        continue;
                    }
                    //2018-9-16 zhengkai 支持'符号以及空格的oracle语句// userid` int(11) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                    String columnName = "";
                    columnLine = columnLine.replaceAll("`", " ").replaceAll("\"", " ").replaceAll("'", "").replaceAll("  ", " ").trim();
                    //如果遇到username varchar(65) default '' not null,这种情况，判断第一个空格是否比第一个引号前
                    try {
                        columnName = columnLine.substring(0, columnLine.indexOf(" ")).get();
                    } catch (StringIndexOutOfBoundsException e) {
                        log.error("err happened: {}", columnLine);
                        throw e;
                    }

                    //默认仅支持驼峰
                    String fieldName = StringUtilsPlus.toLowerCamel(columnName);

                    columnLine = columnLine.substring(columnLine.indexOf("`") + 1).trim();
                    //2025-03-16 修复由于类型大写导致无法转换的问题
                    String mysqlType = columnLine.split("\\s+")[1].toLowerCase(Locale.ROOT);
                    if (mysqlType.contains("(")) {
                        mysqlType = mysqlType.substring(0, mysqlType.indexOf("("));
                    }
                    //mapper.xml class
                    String mapperXmlClass = "VARCHAR";
                    if (MysqlJavaTypeUtil.getMysqlXmlTypeMap().containsKey(mysqlType)) {
                        mapperXmlClass = MysqlJavaTypeUtil.getMysqlXmlTypeMap().get(mysqlType);
                    }
                    //swagger class
                    String swaggerClass = "string";
                    if (MysqlJavaTypeUtil.getMysqlSwaggerTypeMap().containsKey(mysqlType)) {
                        swaggerClass = MysqlJavaTypeUtil.getMysqlSwaggerTypeMap().get(mysqlType);
                    }
                    // field class
                    // int(11) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                    String fieldClass = "String";
                    //2018-9-16 zhengk 补充char/clob/blob/json等类型，如果类型未知，默认为String
                    //2018-11-22 lshz0088 处理字段类型的时候，不严谨columnLine.contains(" int") 类似这种的，可在前后适当加一些空格之类的加以区分，否则当我的字段包含这些字符的时候，产生类型判断问题。
                    //2020-05-03 MOSHOW.K.ZHENG 优化对所有类型的处理
                    //2020-10-20 zhengkai 新增包装类型的转换选择
                    if (MysqlJavaTypeUtil.getMysqlJavaTypeMap().containsKey(mysqlType)) {
                        fieldClass = MysqlJavaTypeUtil.getMysqlJavaTypeMap().get(mysqlType);
                    }
                    // field comment，MySQL的一般位于field行，而pgsql和oralce多位于后面。
                    String fieldComment = null;
                    if (tableSql.contains("comment on column") && (tableSql.contains("." + columnName + " is ") || tableSql.contains(".`" + columnName + "` is"))) {
                        //新增对pgsql/oracle的字段备注支持
                        //COMMENT ON COLUMN public.check_info.check_name IS '检查者名称';
                        //2018-11-22 lshz0088 正则表达式的点号前面应该加上两个反斜杠，否则会认为是任意字符
                        //2019-4-29 zhengkai 优化对oracle注释comment on column的支持（@liukex）
                        tableSql = tableSql.replaceAll(".`" + columnName + "` is", "." + columnName + " is");
                        Matcher columnCommentMatcher = Pattern.compile("\\." + columnName + " is `").matcher(tableSql);
                        fieldComment = columnName;
                        while (columnCommentMatcher.find()) {
                            String columnCommentTmp = columnCommentMatcher.group();
                            //System.out.println(columnCommentTmp);
                            fieldComment = tableSql.substring(tableSql.indexOf(columnCommentTmp) + columnCommentTmp.length()).trim().get();
                            fieldComment = fieldComment.substring(0, fieldComment.indexOf("`")).trim();
                        }
                    } else if (columnLine.contains(" comment")) {
                        //20200518 zhengkai 修复包含comment关键字的问题
                        String commentTmp = columnLine.substring(columnLine.lastIndexOf("comment") + 7).trim().get();
                        // '用户ID',
                        if (commentTmp.contains("`") || commentTmp.indexOf("`") != commentTmp.lastIndexOf("`")) {
                            commentTmp = commentTmp.substring(commentTmp.indexOf("`") + 1, commentTmp.lastIndexOf("`"));
                        }
                        //解决最后一句是评论，无主键且连着)的问题:album_id int(3) default '1' null comment '相册id：0 代表头像 1代表照片墙')
                        if (commentTmp.contains(")")) {
                            commentTmp = commentTmp.substring(0, commentTmp.lastIndexOf(")") + 1);
                        }
                        fieldComment = commentTmp;
                    } else {
                        //修复comment不存在导致报错的问题
                        fieldComment = columnName;
                    }

                    FieldInfo fieldInfo = new FieldInfo();
                    fieldInfo.setColumnName(columnName);
                    fieldInfo.setFieldName(fieldName);
                    fieldInfo.setFieldClass(fieldClass);
                    fieldInfo.setSwaggerClass(swaggerClass);
                    fieldInfo.setFieldXmlClass(mapperXmlClass);
                    fieldInfo.setFieldComment(fieldComment);

                    fieldList.add(fieldInfo);
                }
            }
        }

        if (fieldList.size() < 1) {
            throw new CodeGenerateException("表结构分析失败，请检查语句或者提交issue给我");
        }


        ClassInfo codeJavaInfo = new ClassInfo();
        codeJavaInfo.setTableName(tableName);
        codeJavaInfo.setClassName(className);
        codeJavaInfo.setClassComment(classComment);
        codeJavaInfo.setFieldList(fieldList);
        codeJavaInfo.setOriginTableName(originTableName);
        codeJavaInfo.setTableComment(classComment);

//        codeJavaInfo.setClassNameWithSuffix(codeJavaInfo.getClassName() + tableConfig.getDtoSuffix());
        codeJavaInfo.setVoPackageName(tableConfig.getVoPackageName() + "." + codeJavaInfo.getClassName()+"VO");
        codeJavaInfo.setDtoPackageName(tableConfig.getDtoPackageName() + "." + codeJavaInfo.getClassName()+tableConfig.getDtoSuffix());
        codeJavaInfo.setMapperPackageName(tableConfig.getMapperPackageName() + "." + codeJavaInfo.getClassName());
        codeJavaInfo.setControllerPackageName(tableConfig.getControllerPackageName() );


        return codeJavaInfo;
    }

    private NonCaseString getTableSql(TableConfig tableConfig) {
        //process the param
        NonCaseString tableSql = NonCaseString.of(tableConfig.getSql());
//            String nameCaseType = MapUtil.getString(paramInfo.getOptions(),"nameCaseType");

        //更新空值处理
        if (StringUtils.isBlank(tableSql)) {
            throw new CodeGenerateException("Table structure can not be empty. 表结构不能为空。");
        }
        //deal with special character
        tableSql = tableSql.trim()
                .replaceAll("'", "`")
                .replaceAll("\"", "`")
                .replaceAll("，", ",")
        // 这里全部转小写, 会让驼峰风格的字段名丢失驼峰信息(真有驼峰sql字段名的呢(*￣︶￣)); 下文使用工具方法处理包含等
        // .toLowerCase()
        ;
        //deal with java string copy \n"
        tableSql = tableSql.trim().replaceAll("\\\\n`", "").replaceAll("\\+", "").replaceAll("``", "`").replaceAll("\\\\", "");
        return tableSql;
    }

    /**
     * @return
     * @Author zhouchuanxiang
     * @Description 获取表的备注
     * @Date 17:11 2025/9/28
     * @Param [tableSql, className, tableName]
     **/
    private String getClassComment(NonCaseString tableSql, String className, String tableName) {
        String classComment = null;
        //mysql是comment=,pgsql/oracle是comment on table,
        // 定义正则表达式  comment 和 =  之间可能包含多个空格
        String regex = "comment\\s*=";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(tableSql);
        if (matcher.find()) {

//        if (tableSql.containsAny("comment=", "comment on table")) {
            int ix = tableSql.lastIndexOf("comment");
            String classCommentTmp = (ix > -1) ?
                    tableSql.substring(ix + 8).trim().get() :
                    tableSql.substring(tableSql.lastIndexOf("comment on table") + 17).trim().get();
            if (classCommentTmp.contains("`")) {
                classCommentTmp = classCommentTmp.substring(classCommentTmp.indexOf("`") + 1);
                classCommentTmp = classCommentTmp.substring(0, classCommentTmp.indexOf("`"));
                classComment = classCommentTmp;
            } else {
                //非常规的没法分析
                classComment = className;
            }
        } else {
            //修复表备注为空问题
            classComment = tableName;
        }
        //如果备注跟;混在一起，需要替换掉
        classComment = classComment.replaceAll(";", "");
        return classComment;
    }

    /**
     * @return
     * @Author zhouchuanxiang
     * @Description 获取原始表名
     * @Date 17:09 2025/9/28
     * @Param [tableSql]
     **/
    private String getOriginTableName(NonCaseString tableSql) {
        String tableName = null;
        int tableKwIx = tableSql.indexOf("TABLE"); // 包含判断和位置一次搞定
        if (tableKwIx > -1 && tableSql.contains("(")) {
            tableName = tableSql.substring(tableKwIx + 5, tableSql.indexOf("(")).get();
        } else {
            throw new CodeGenerateException("Table structure incorrect.表结构不正确。");
        }

        //新增处理create table if not exists members情况
        if (tableName.contains("if not exists")) {
            tableName = tableName.replaceAll("if not exists", "");
        }

        if (tableName.contains("`")) {
            tableName = tableName.substring(tableName.indexOf("`") + 1, tableName.lastIndexOf("`"));
        } else {
            //空格开头的，需要替换掉\n\t空格
            tableName = tableName.replaceAll(" ", "").replaceAll("\n", "").replaceAll("\t", "");
        }
        //优化对byeas`.`ct_bd_customerdiscount这种命名的支持
        if (tableName.contains("`.`")) {
            tableName = tableName.substring(tableName.indexOf("`.`") + 3);
        } else if (tableName.contains(".")) {
            //优化对likeu.members这种命名的支持
            tableName = tableName.substring(tableName.indexOf(".") + 1);
        }
        return tableName;
    }


    private boolean isNotSpecialColumnLine(NonCaseString columnLine, int lineSeq) {
        return (
                !columnLine.containsAny(
                        "key ",
                        "constraint",
                        " using ",
                        "unique ",
                        "fulltext ",
                        "index ",
                        "pctincrease",
                        "buffer_pool",
                        "tablespace"
                )
                        && !(columnLine.contains("primary ") && columnLine.indexOf("storage") + 3 > columnLine.indexOf("("))
                        && !(columnLine.contains("primary ") && lineSeq > 3)
        );
    }

    String templateConfig = null;

    /**
     * 从项目中的JSON文件读取String
     *
     * @author 
     */
//    @Override
    public String getTemplateConfig() throws IOException {
        templateConfig = null;
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template-riskproduct.json");
        templateConfig = new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining(System.lineSeparator()));
        inputStream.close();
        //log.info(JSON.toJSONString(templateCpnfig));
        return templateConfig;
    }

    /**
     * 替换所有COMMENT中的英文逗号为中文逗号
     */
    public String replaceCommentCommas(String sql) {
        // 正则表达式匹配 COMMENT '...' 中的内容
//        Pattern pattern = Pattern.compile("comment\\s+'([^']*)'");
        Pattern pattern = Pattern.compile("comment\\s+(?:=\\s*)?`([^`]*)`");
        Matcher matcher = pattern.matcher(sql);

        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            // 获取注释内容并替换英文逗号
            String commentContent = matcher.group(1);
            String replacedComment = commentContent.replace(",", "，");

            // 重新构建匹配部分
            String replacement = "COMMENT '" + replacedComment + "'";
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);

        return result.toString();
    }
}
