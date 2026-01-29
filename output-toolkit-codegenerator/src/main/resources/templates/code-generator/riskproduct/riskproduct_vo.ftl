<#if classInfo.dtoPackageName??>
package ${classInfo.voPackageName};

</#if>

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import lombok.Data;
import ${classInfo.dtoPackageName}.${classInfo.className}${dtoSuffix};

/**
 * @description  ${classInfo.className}${dtoSuffix} 的属性拓展VO   ${classInfo.classComment}   对应表：${classInfo.originTableName}
 * @author ${authorName}
 * @date ${.now?string('yyyy-MM-dd')}
 * @fileName ${classInfo.className}VO
 */
@Data
public class ${classInfo.className}VO extends ${classInfo.className}${dtoSuffix} implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
    * 页大小
    */
    private Integer pageSize = 10;

    /**
    * 起始页
    */
    private Integer pageStart = 1;

}
