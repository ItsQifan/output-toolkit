<#if classInfo.mapperPackageName??>
package ${classInfo.mapperPackageName}Mapper;

</#if>
<#--<#if isAutoImport?exists && isAutoImport==true>-->
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import java.util.List;
<#--</#if>-->

/**
 * @description ${classInfo.classComment}    对应表：${classInfo.originTableName}
 * @author ${authorName}
 * @date ${.now?string('yyyy-MM-dd')}
 */
@Mapper
public interface ${classInfo.className}${mapperSuffix} {

    /**
    * @description 新增
    * @author ${authorName}
    * @date ${.now?string('yyyy/MM/dd')}
    **/
    int insert(${classInfo.className}${dtoSuffix} ${classInfo.className?uncap_first}${dtoSuffix});

    /**
    * @description 刪除
    * @author ${authorName}
    * @date ${.now?string('yyyy/MM/dd')}
    **/
    int delete(String id);

    /**
    * @description 更新
    * @author ${authorName}
    * @date ${.now?string('yyyy/MM/dd')}
    **/
    int update(${classInfo.className}${dtoSuffix} ${classInfo.className?uncap_first}${dtoSuffix});

    /**
    * @description 查询 根据主键 id 查询
    * @author ${authorName}
    * @date ${.now?string('yyyy/MM/dd')}
    **/
    ${classInfo.className}${dtoSuffix} queryById(String id);

    /**
    * @description 查询 分页查询
    * @author ${authorName}
    * @date ${.now?string('yyyy/MM/dd')}
    **/
    List<${classInfo.className}${dtoSuffix}> pageList(${classInfo.className}VO ${classInfo.className?uncap_first});



}
