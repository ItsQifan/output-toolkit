<#if classInfo.dtoPackageName??>
package ${classInfo.dtoPackageName};

</#if>
import com.bestpay.riskcontrol.riskproduct.api.entity.BaseDTO;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 * @description ${classInfo.classComment}   对应表：${classInfo.originTableName}
 * @author ${authorName}
 * @date ${.now?string('yyyy-MM-dd')}
 * @fileName ${classInfo.className}${dtoSuffix}
 */
@Data
public class ${classInfo.className}${dtoSuffix} extends BaseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

<#if classInfo.fieldList?exists && classInfo.fieldList?size gt 0>
<#-- 定义要排除的字段列表 -->
    <#assign excludeFields = ["id", "createdAt", "createdBy", "updatedAt","updatedBy"]>

<#list classInfo.fieldList as fieldItem >
    <#if !excludeFields?seq_contains(fieldItem.fieldName)>
    /**
    * ${fieldItem.fieldComment}
    */
    private ${fieldItem.fieldClass} ${fieldItem.fieldName};
    </#if>
</#list>
</#if>

<#if classInfo.fieldList?exists && classInfo.fieldList?size gt 0>
    public ${classInfo.className}${dtoSuffix}() {
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
