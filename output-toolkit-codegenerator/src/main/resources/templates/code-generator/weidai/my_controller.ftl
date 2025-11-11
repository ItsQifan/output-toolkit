<#if controllerPackageName??>
    package ${controllerPackageName}.${classInfo.className}Controller;

</#if>
<#--<#if isAutoImport?exists && isAutoImport==true>-->
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
<#--</#if>-->

/**
* @description ${classInfo.classComment}
* @author ${authorName}
* @date ${.now?string('yyyy-MM-dd')}
* @fileName ${classInfo.className}Controller
*/
@RestController
@RequestMapping(value = "/${classInfo.className?uncap_first}")
@Slf4j
public class ${classInfo.className}Controller {

@Autowired
private ${classInfo.className}${managerSuffix} ${classInfo.className?uncap_first}${managerSuffix};

/**
* 新增
* @author ${authorName}
* @date ${.now?string('yyyy/MM/dd')}
**/
@RequestMapping("/insert")
@MethodAspectAnnotation()
@ControllerAnnotation("新增单条")
public Object insert(${classInfo.className} ${classInfo.className?uncap_first}){
return ${classInfo.className?uncap_first}Service.insert(${classInfo.className?uncap_first});
}

/**
* 刪除
* @author ${authorName}
* @date ${.now?string('yyyy/MM/dd')}
**/
@RequestMapping("/delete")
public Object delete(int id){
return ${classInfo.className?uncap_first}Service.delete(id);
}

/**
* 更新
* @author ${authorName}
* @date ${.now?string('yyyy/MM/dd')}
**/
@RequestMapping("/update")
public Object update(${classInfo.className} ${classInfo.className?uncap_first}){
return ${classInfo.className?uncap_first}Service.update(${classInfo.className?uncap_first});
}

/**
* 查询 根据主键 id 查询
* @author ${authorName}
* @date ${.now?string('yyyy/MM/dd')}
**/
@RequestMapping("/queryById")
public Object queryById(int id){
return ${classInfo.className?uncap_first}Service.queryById(id);
}

/**
* 查询 分页查询
* @author ${authorName}
* @date ${.now?string('yyyy/MM/dd')}
**/
@RequestMapping("/pageList")
public Map<String, Object> pageList(@RequestParam(required = false, defaultValue = "0") int offset,
@RequestParam(required = false, defaultValue = "10") int pagesize) {
return ${classInfo.className?uncap_first}Service.pageList(offset, pagesize);
}

}
