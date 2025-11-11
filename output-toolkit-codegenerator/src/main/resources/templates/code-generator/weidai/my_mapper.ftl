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
int insert(${classInfo.classNameWithSuffix} ${classInfo.className?uncap_first});

/**
* @description 刪除
* @author ${authorName}
* @date ${.now?string('yyyy/MM/dd')}
**/
int delete(int id);

/**
* @description 更新
* @author ${authorName}
* @date ${.now?string('yyyy/MM/dd')}
**/
int update(${classInfo.classNameWithSuffix} ${classInfo.className?uncap_first});

/**
* @description 查询 根据主键 id 查询
* @author ${authorName}
* @date ${.now?string('yyyy/MM/dd')}
**/
${classInfo.className} load(int id);

/**
* @description 查询 分页查询
* @author ${authorName}
* @date ${.now?string('yyyy/MM/dd')}
**/
List<${classInfo.classNameWithSuffix}> pageList(int offset,int pagesize);

/**
* @description 查询 分页查询 count
* @author ${authorName}
* @date ${.now?string('yyyy/MM/dd')}
**/
int pageListCount(int offset,int pagesize);

}
