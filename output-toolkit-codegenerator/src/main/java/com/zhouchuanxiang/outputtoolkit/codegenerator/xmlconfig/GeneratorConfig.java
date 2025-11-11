package com.zhouchuanxiang.outputtoolkit.codegenerator.xmlconfig;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.List;

@Data
@JacksonXmlRootElement(localName = "config")
public class GeneratorConfig {

    //包装元素的标签名称
    @JacksonXmlElementWrapper(localName = "tableConfigList")
    //每个元素的标签名称
    @JacksonXmlProperty(localName = "tableConfig")
    private List<TableConfig> tableConfigList;
}
