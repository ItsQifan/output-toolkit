package com.zhouchuanxiang.outputtoolkit.codegenerator.controller;


import com.alibaba.fastjson2.JSON;
import com.zhouchuanxiang.outputtoolkit.codegenerator.entity.ReturnT;
import com.zhouchuanxiang.outputtoolkit.codegenerator.service.XmlGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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



}
