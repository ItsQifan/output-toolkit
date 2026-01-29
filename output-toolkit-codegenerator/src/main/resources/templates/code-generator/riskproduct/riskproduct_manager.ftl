<#-- 生成Manager接口 -->
import java.util.List;

/**
* @description ${classInfo.className}${managerSuffix}接口
* @author ${authorName!"zhouchuanxiang"}
* @date ${.now?string("yyyy-MM-dd")}
*/
public interface ${classInfo.className}${managerSuffix} {

    /**
    * 新增单条记录
    * @author ${authorName}
    * @date ${.now?string('yyyy/MM/dd')}
    * @param ${classInfo.className?uncap_first}
    * @return 是否成功
    */
    Boolean insert(${classInfo.className}VO ${classInfo.className?uncap_first});

    /**
    * 根据ID删除单条记录
    * @author ${authorName}
    * @date ${.now?string('yyyy/MM/dd')}
    * @param ${classInfo.className?uncap_first}
    * @return 是否成功
    */
    Boolean delete(${classInfo.className}VO ${classInfo.className?uncap_first});

    /**
    * 更新记录
    * @author ${authorName}
    * @date ${.now?string('yyyy/MM/dd')}
    * @param ${classInfo.className?uncap_first}
    * @return 是否成功
    */
    Boolean update(${classInfo.className}VO ${classInfo.className?uncap_first});

    /**
    * 根据主键id查询
    * @author ${authorName}
    * @date ${.now?string('yyyy/MM/dd')}
    * @param ${classInfo.className?uncap_first}
    * @return 实体对象
    */
    ${classInfo.className}VO queryById(${classInfo.className}VO ${classInfo.className?uncap_first});

    /**
    * 分页查询
    * @author ${authorName}
    * @date ${.now?string('yyyy/MM/dd')}
    * @param ${classInfo.className?uncap_first}
    * @return 分页结果
    */
    PageInfo<${classInfo.className}VO>  pageList(${classInfo.className}VO ${classInfo.className?uncap_first});

}
