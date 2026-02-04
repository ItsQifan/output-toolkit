import com.alibaba.fastjson2.JSON;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.BeanUtils;
//import com.bp.riskcontrol.riskproduct.api.utils.RequestMsgUtil;
import com.bestpay.riskcontrol.riskproduct.api.utils.RequestMsgUtil;
import com.bestpay.riskcontrol.riskproduct.api.utils.ValidateUtil;
import com.bestpay.riskcontrol.riskproduct.api.excepiton.BizErrorCode;
import ${classInfo.mapperPackageName}Mapper;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import com.github.pagehelper.PageHelper;

import java.util.Objects;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
/**
* @description ${classInfo.className}${managerSuffix}接口实现类
* @author ${authorName}
* @date ${.now?string("yyyy-MM-dd")}
*/

@Slf4j
@Component
public class ${classInfo.className}${managerSuffix} {

    <#-- 依赖注入DAO -->
    @Autowired
    private ${classInfo.className}${mapperSuffix} ${classInfo.className?uncap_first}${mapperSuffix};

    /**
    * 新增单条记录
    * @author ${authorName}
    * @date ${.now?string('yyyy/MM/dd')}
    * @param ${classInfo.className?uncap_first}
    * @return
    */
    @Transactional
    public Boolean insert(${classInfo.className}VO ${classInfo.className?uncap_first}) {
        log.info("${classInfo.className}${managerSuffix}.insert方法开始，参数：{}", JSON.toJSONString(${classInfo.className?uncap_first}));
        //参数校验
        ValidateUtil.paramValidate(Objects.isNull(${classInfo.className?uncap_first}),BizErrorCode.BLANK_IS_ILLEGAL_PARAM, "参数不能为空！");
        //ValidateUtil.validateString(${classInfo.className?uncap_first}.getId(),BizErrorCode.BLANK_IS_ILLEGAL_PARAM, "string类型的id不能为空！");
        //ValidateUtil.paramValidate(Objects.isNull(${classInfo.className?uncap_first}.getId()),BizErrorCode.BLANK_IS_ILLEGAL_PARAM, "long类型的id不能为空！");
        //ValidateUtil.paramValidate(CollectionUtils.isEmpty(list), BizErrorCode.BLANK_IS_ILLEGAL_PARAM, "list不能为空！");

        //vo转DTO
        ${classInfo.className}${dtoSuffix} ${classInfo.className?uncap_first}${dtoSuffix} = new ${classInfo.className}${dtoSuffix}();
        BeanUtils.copyProperties(${classInfo.className?uncap_first},${classInfo.className?uncap_first}${dtoSuffix});
        ${classInfo.className?uncap_first}${dtoSuffix}.setCreatedAt(new Date());
        ${classInfo.className?uncap_first}${dtoSuffix}.setCreatedBy(RequestMsgUtil.getSessionUser().getUserName());
        int result = ${classInfo.className?uncap_first}${mapperSuffix}.insert(${classInfo.className?uncap_first}${dtoSuffix});
        log.info("${classInfo.className}${managerSuffix}.insert方法结束");
        return result > 0;
    }

    /**
    * 根据ID删除单条记录
    * @author ${authorName}
    * @date ${.now?string('yyyy/MM/dd')}
    * @param ${classInfo.className?uncap_first}
    * @return
    */
    @Transactional
    public Boolean delete(${classInfo.className}VO ${classInfo.className?uncap_first}) {
        log.info("${classInfo.className}${managerSuffix}.delete方法开始，参数：{}", JSON.toJSONString(${classInfo.className?uncap_first}));
        //参数校验
        ValidateUtil.paramValidate(Objects.isNull(${classInfo.className?uncap_first}),BizErrorCode.BLANK_IS_ILLEGAL_PARAM, "参数不能为空！");
        //ValidateUtil.validateString(${classInfo.className?uncap_first}.getId(),BizErrorCode.BLANK_IS_ILLEGAL_PARAM, "string类型的id不能为空！");
        //ValidateUtil.paramValidate(Objects.isNull(${classInfo.className?uncap_first}.getId()),BizErrorCode.BLANK_IS_ILLEGAL_PARAM, "long类型的id不能为空！");

        int result = ${classInfo.className?uncap_first}${mapperSuffix}.delete(${classInfo.className?uncap_first}.getId());

        log.info("${classInfo.className}${managerSuffix}.delete方法结束");
        return result > 0;
    }

    /**
    * 更新记录
    * @author ${authorName}
    * @date ${.now?string('yyyy/MM/dd')}
    * @param ${classInfo.className?uncap_first}
    * @return
    */
    @Transactional
    public Boolean update(${classInfo.className}VO ${classInfo.className?uncap_first}) {
        log.info("${classInfo.className}${managerSuffix}.update方法开始，参数：{}",  JSON.toJSONString(${classInfo.className?uncap_first}));
        //参数校验
        ValidateUtil.paramValidate(Objects.isNull(${classInfo.className?uncap_first}),BizErrorCode.BLANK_IS_ILLEGAL_PARAM, "参数不能为空！");
        //ValidateUtil.validateString(${classInfo.className?uncap_first}.getId(),BizErrorCode.BLANK_IS_ILLEGAL_PARAM, "string类型的id不能为空！");
        //ValidateUtil.paramValidate(Objects.isNull(${classInfo.className?uncap_first}.getId()),BizErrorCode.BLANK_IS_ILLEGAL_PARAM, "long类型的id不能为空！");

        //vo转DTO
        ${classInfo.className}${dtoSuffix} ${classInfo.className?uncap_first}${dtoSuffix} = new ${classInfo.className}${dtoSuffix}();
        BeanUtils.copyProperties(${classInfo.className?uncap_first},${classInfo.className?uncap_first}${dtoSuffix});
        ${classInfo.className?uncap_first}${dtoSuffix}.setUpdatedAt(new Date());
        ${classInfo.className?uncap_first}${dtoSuffix}.setUpdatedBy(RequestMsgUtil.getSessionUser().getUserName());
        int result = ${classInfo.className?uncap_first}${mapperSuffix}.update(${classInfo.className?uncap_first}${dtoSuffix});
        log.info("${classInfo.className}${managerSuffix}.update方法结束");
        return result > 0;

    }

    /**
    * 根据主键id查询
    * @author ${authorName}
    * @date ${.now?string('yyyy/MM/dd')}
    * @param ${classInfo.className?uncap_first}
    * @return
    */
    public ${classInfo.className}VO queryById(${classInfo.className}VO ${classInfo.className?uncap_first}) {
        log.info("${classInfo.className}${managerSuffix}.queryById方法开始，参数：{}", JSON.toJSONString(${classInfo.className?uncap_first}));
        //参数校验
        ValidateUtil.paramValidate(Objects.isNull(${classInfo.className?uncap_first}),BizErrorCode.BLANK_IS_ILLEGAL_PARAM, "参数不能为空！");
        //ValidateUtil.validateString(${classInfo.className?uncap_first}.getId(),BizErrorCode.BLANK_IS_ILLEGAL_PARAM, "string类型的id不能为空！");
        //ValidateUtil.paramValidate(Objects.isNull(${classInfo.className?uncap_first}.getId()),BizErrorCode.BLANK_IS_ILLEGAL_PARAM, "long类型的id不能为空！");

        ${classInfo.className}${dtoSuffix} ${classInfo.className?uncap_first}${dtoSuffix} = ${classInfo.className?uncap_first}${mapperSuffix}.queryById(${classInfo.className?uncap_first}.getId());
        ${classInfo.className}VO ${classInfo.className?uncap_first}VO = new ${classInfo.className}VO();
        BeanUtils.copyProperties(${classInfo.className?uncap_first}${dtoSuffix},${classInfo.className?uncap_first}VO);
        log.info("${classInfo.className}${managerSuffix}.queryById方法结束");
        return ${classInfo.className?uncap_first}VO;
    }

    /**
    * 分页查询
    * @author ${authorName}
    * @date ${.now?string('yyyy/MM/dd')}
    * @param ${classInfo.className?uncap_first}
    * @return
    */
    public PageInfo<${classInfo.className}VO> pageList(${classInfo.className}VO ${classInfo.className?uncap_first}) {
        log.info("${classInfo.className}${managerSuffix}.pageList方法开始，参数：{}", JSON.toJSONString(${classInfo.className?uncap_first}));
        //参数校验
        //ValidateUtil.paramValidate(Objects.isNull(${classInfo.className?uncap_first}),BizErrorCode.BLANK_IS_ILLEGAL_PARAM, "参数不能为空！");
        //ValidateUtil.validateString(${classInfo.className?uncap_first}.getId(),BizErrorCode.BLANK_IS_ILLEGAL_PARAM, "string类型的id不能为空！");
        //ValidateUtil.paramValidate(Objects.isNull(${classInfo.className?uncap_first}.getId()),BizErrorCode.BLANK_IS_ILLEGAL_PARAM, "long类型的id不能为空！");

        PageHelper.startPage(${classInfo.className?uncap_first}.getPageStart(), ${classInfo.className?uncap_first}.getPageSize());
        List<${classInfo.className}${dtoSuffix}> dtoList = ${classInfo.className?uncap_first}${mapperSuffix}.pageList(${classInfo.className?uncap_first});
        PageInfo<${classInfo.className}${dtoSuffix}> dtoPageInfo = new PageInfo<>(dtoList);

        List<${classInfo.className}VO> voList = dtoList.stream()
            .map(dto -> {
                ${classInfo.className}VO vo = new ${classInfo.className}VO();
                BeanUtils.copyProperties(dto,vo);
                return vo;
            })
            .collect(Collectors.toList());
        log.info("${classInfo.className}${managerSuffix}.queryById方法结束,list.size={}", voList.size());
        PageInfo<${classInfo.className}VO> voPageInfo = new PageInfo<>();
        BeanUtils.copyProperties(dtoPageInfo,voPageInfo);
        voPageInfo.setList(voList);
        return voPageInfo;
    }
}