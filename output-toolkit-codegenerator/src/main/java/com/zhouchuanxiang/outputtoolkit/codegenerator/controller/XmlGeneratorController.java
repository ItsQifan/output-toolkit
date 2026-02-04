package com.zhouchuanxiang.outputtoolkit.codegenerator.controller;


import com.alibaba.fastjson2.JSON;
import com.zhouchuanxiang.outputtoolkit.codegenerator.entity.ReturnT;
import com.zhouchuanxiang.outputtoolkit.codegenerator.service.XmlGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.openjdk.jmh.profile.PausesProfiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author zhouchuanxiang
 * @Description  xml 方式生成器
 * @Date 16:13 2025/9/28
 * @Param
 * @return
 **/
@Slf4j
@RestController
@RequestMapping("/xmlGen")
public class XmlGeneratorController {


    @Autowired
    private XmlGeneratorService xmlGeneratorService;


    /**
     * @Author zhouchuanxiang
     * @Description  生成xml 配置文件
     * @Date 09:54 2025/9/30
     * @Param []
     * @return
     **/
    @PostMapping("/generateXml")
    public ReturnT generateXml() throws Exception {
        log.info("generateXml start启动");
        String xmlpath = xmlGeneratorService.generateXml();
        log.info("generateXml end,xmlpath={}", xmlpath);
        log.info("xmlpath JSONString:"+ JSON.toJSONString(xmlpath));
        return ReturnT.ok().put("data", xmlpath);
    }

    /**
     * @Author zhouchuanxiang
     * @Description 执行
     * @Date 16:13 2025/9/28
     * @Param []
     * @return
     **/
    @PostMapping("/doGenerate")
    public ReturnT generateCodeByXml(@RequestBody Map<String,String> paramMap) throws Exception {
        log.info("generateCodeByXml start，generatorConfigPath={}", paramMap);
        xmlGeneratorService.doGenerate(paramMap);
        return ReturnT.ok();
    }


    /**
     * 【=========推荐一步执行============】
     * @Author zhouchuanxiang
     * @Description  生成xml配置文件，并执行
     * @Date 13:57 2025/11/11
     * @Param []
     * @return
     **/
    @PostMapping("/generateXmlAndDoGenerate")
    public ReturnT generateXmlAndDoGenerate() throws Exception {
        log.info("generateXmlAndDoGenerate start启动");
        String xmlpath = xmlGeneratorService.generateXml();
        log.info("generateXmlAndDoGenerate end,xmlpath={}", xmlpath);
        log.info("generateXmlAndDoGenerate JSONString:"+ JSON.toJSONString(xmlpath));

        Map<String,String> paramMap=new HashMap<>();
        paramMap.put("generatorConfigPath",xmlpath);
        xmlGeneratorService.doGenerate(paramMap);
        log.info("generateXmlAndDoGenerate end");
        return ReturnT.ok();
    }



    //场景：如果数据库新增字段或者删除了字段， 需要重新生成
    //=========增量更新逻辑：=======================
    //1-DTO重新生成
    //2-controller/manager/VO不变
    //3-mapper.xml针对性的更新
    //【实现原理（以DTO举例）：根据建表语句会生成DTO代码，读取第一次生成的DTO代码文件，字段对比】
    /**
     * @Author zhouchuanxiang
     * @Description 增量更新代码文件（支持字段新增/删除）
     *              只更新DTO、VO、Mapper.xml，Controller和Manager需要手动检查
     * @Date 2026-02-04
     * @Param [paramMap] 包含generatorConfigPath的参数Map
     * @return ReturnT
     **/
    @PostMapping("/incrementalUpdate")
    public ReturnT incrementalUpdate(@RequestBody Map<String,String> paramMap) throws Exception {
        log.info("incrementalUpdate start，generatorConfigPath={}", paramMap.get("generatorConfigPath"));
        String result = xmlGeneratorService.incrementalUpdate(paramMap.get("generatorConfigPath"));
        log.info("incrementalUpdate end，result={}", result);
        return ReturnT.ok().put("data", result);
    }

    /**
     * 【=========增量 推荐一步执行============】
     * @Author zhouchuanxiang
     * @Description 生成xml配置文件，并执行增量更新
     * @Date 2026-02-04
     * @Param []
     * @return ReturnT
     **/
    @PostMapping("/generateXmlAndIncrementalUpdate")
    public ReturnT generateXmlAndIncrementalUpdate() throws Exception {
        log.info("generateXmlAndIncrementalUpdate start启动");
        
        // 1. 生成XML配置文件
        String xmlpath = xmlGeneratorService.generateXml();
        log.info("生成XML配置文件完成，xmlpath={}", xmlpath);
        
        // 2. 执行增量更新
        String result = xmlGeneratorService.incrementalUpdate(xmlpath);
        log.info("增量更新完成，result={}", result);
        
        return ReturnT.ok().put("data", result);
    }



}
