<#if classInfo.dtoPackageName??>
    package ${classInfo.dtoPackageName};

</#if>

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import lombok.Data;

/**
* @description ${classInfo.classComment}   对应表：${classInfo.originTableName}
* @author ${authorName}
* @date ${.now?string('yyyy-MM-dd')}
* @fileName ${classInfo.classNameWithSuffix}
*/
@Data
public class ${classInfo.classNameWithSuffix} implements Serializable {

private static final long serialVersionUID = 1L;

<#if classInfo.fieldList?exists && classInfo.fieldList?size gt 0>
    <#list classInfo.fieldList as fieldItem >
        /**
        * ${fieldItem.fieldComment}
        */
        private ${fieldItem.fieldClass} ${fieldItem.fieldName};

    </#list>
</#if>

<#if classInfo.fieldList?exists && classInfo.fieldList?size gt 0>
    public ${classInfo.classNameWithSuffix}() {
    }

<#--get set  方法-->
<#--<#list classInfo.fieldList as fieldItem>-->
<#--    public ${fieldItem.fieldClass} get${fieldItem.fieldName?cap_first}() {-->
<#--        return ${fieldItem.fieldName};-->
<#--    }-->

<#--    public void set${fieldItem.fieldName?cap_first}(${fieldItem.fieldClass} ${fieldItem.fieldName}) {-->
<#--        this.${fieldItem.fieldName} = ${fieldItem.fieldName};-->
<#--    }-->

<#--</#list>-->
</#if>
}
