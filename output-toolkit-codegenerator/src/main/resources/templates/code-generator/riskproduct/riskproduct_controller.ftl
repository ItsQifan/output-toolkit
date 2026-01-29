<#if controllerPackageName??>
package ${controllerPackageName}.${classInfo.className}Controller;

</#if>
<#--<#if isAutoImport?exists && isAutoImport==true>-->
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestBody;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import com.bestpay.riskcontrol.riskproduct.cm.annotation.ControllerAnnotation;
import com.github.pagehelper.PageInfo;
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
    @ControllerAnnotation("新增单条")
    public Boolean insert(@RequestBody ${classInfo.className}VO ${classInfo.className?uncap_first}){
        return ${classInfo.className?uncap_first}${managerSuffix}.insert(${classInfo.className?uncap_first});
    }

    /**
    * 根据ID删除单条
    * @author ${authorName}
    * @date ${.now?string('yyyy/MM/dd')}
    **/
    @RequestMapping("/delete")
    @ControllerAnnotation("根据ID删除单条")
    public Boolean delete(@RequestBody ${classInfo.className}VO ${classInfo.className?uncap_first}){
        return ${classInfo.className?uncap_first}${managerSuffix}.delete(${classInfo.className?uncap_first});
    }

    /**
    * 更新
    * @author ${authorName}
    * @date ${.now?string('yyyy/MM/dd')}
    **/
    @RequestMapping("/update")
    @ControllerAnnotation("更新")
    public Boolean update(@RequestBody ${classInfo.className}VO ${classInfo.className?uncap_first}){
        return ${classInfo.className?uncap_first}${managerSuffix}.update(${classInfo.className?uncap_first});
    }

    /**
    * 查询 根据主键 id 查询
    * @author ${authorName}
    * @date ${.now?string('yyyy/MM/dd')}
    **/
    @RequestMapping("/queryById")
    @ControllerAnnotation("根据主键id查询")
    public ${classInfo.className}VO queryById(@RequestBody ${classInfo.className}VO ${classInfo.className?uncap_first}){
        return ${classInfo.className?uncap_first}${managerSuffix}.queryById(${classInfo.className?uncap_first});
    }

    /**
    * 查询 分页查询
    * @author ${authorName}
    * @date ${.now?string('yyyy/MM/dd')}
    **/
    @RequestMapping("/pageList")
    @ControllerAnnotation("分页查询")
    public PageInfo<${classInfo.className}VO> pageList(@RequestBody ${classInfo.className}VO ${classInfo.className?uncap_first}) {
        return ${classInfo.className?uncap_first}${managerSuffix}.pageList(${classInfo.className?uncap_first});
    }

}
