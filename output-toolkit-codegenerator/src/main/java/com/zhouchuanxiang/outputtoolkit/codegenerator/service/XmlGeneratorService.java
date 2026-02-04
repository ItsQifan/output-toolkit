package com.zhouchuanxiang.outputtoolkit.codegenerator.service;


import com.zhouchuanxiang.outputtoolkit.codegenerator.entity.ClassInfo;

import java.util.List;
import java.util.Map;

/**
 * @Author zhouchuanxiang
 * @Description  通过想，xml实现
 * @Date 15:28 2025/9/26
 * @Param
 * @return
 **/
public interface XmlGeneratorService {


    /**
     * @Author zhouchuanxiang
     * @Description  执行生成
     * @Date 15:31 2025/9/26
     * @Param []
     * @return
     **/
    public List<ClassInfo> doGenerate (Map<String,String> paramMap) throws Exception ;

    /**
     * @Author zhouchuanxiang
     * @Description  生成xml配置文件
     * @Date 14:30 2025/9/29
     * @Param []
     * @return
     **/
    String generateXml();

    /**
     * @Author zhouchuanxiang
     * @Description  增量更新代码文件（支持字段新增/删除）
     * @Date 2026-02-04
     * @Param [xmlConfigPath] XML配置文件路径
     * @return 更新结果描述
     **/
    String incrementalUpdate(String xmlConfigPath) throws Exception;

}
