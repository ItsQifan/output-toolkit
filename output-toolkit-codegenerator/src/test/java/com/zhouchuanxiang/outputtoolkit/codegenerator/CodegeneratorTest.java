package com.zhouchuanxiang.outputtoolkit.codegenerator;

import com.alibaba.fastjson2.JSON;
import com.zhouchuanxiang.outputtoolkit.codegenerator.service.XmlGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@Slf4j
public class CodegeneratorTest {


    @Autowired
    private XmlGeneratorService xmlGeneratorService;


    /**
     * @Author zhouchuanxiang
     * @Description  【生成xml配置文件  并   生成代码】
     * @Date 14:07 2025/11/11
     * @Param []
     * @return
     **/
    @Test
    public void testGenerateXmlAndDoGenerate() throws Exception {
        log.info("testGenerateXmlAndDoGenerate start");
        String xmlpath = xmlGeneratorService.generateXml();
        log.info("generateXmlAndDoGenerate end,xmlpath={}", xmlpath);
        log.info("generateXmlAndDoGenerate JSONString:"+ JSON.toJSONString(xmlpath));

        Map<String,String> paramMap=new HashMap<>();
        paramMap.put("generatorConfigPath",xmlpath);
        xmlGeneratorService.doGenerate(paramMap);
        log.info("testGenerateXmlAndDoGenerate end");

    }
}
