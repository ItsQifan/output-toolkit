package com.zhouchuanxiang.outputtoolkit.codegenerator.service;

import com.zhouchuanxiang.outputtoolkit.codegenerator.entity.ClassInfo;

/**
 * @Author zhouchuanxiang
 * @Description 增量更新服务接口 - 支持字段的新增和删除
 * @Date 2026-02-04
 */
public interface IncrementalUpdateService {

    /**
     * 增量更新已存在的代码文件（DTO、VO、Mapper.xml）
     * Controller 和 Manager 跳过不处理
     * 
     * @param xmlConfigPath XML配置文件路径
     * @return 更新结果描述
     * @throws Exception 更新失败时抛出异常
     */
    String incrementalUpdate(String xmlConfigPath) throws Exception;

    /**
     * 更新DTO文件 - 添加新字段或删除旧字段
     * 
     * @param filePath DTO文件路径
     * @param newClassInfo 新的类信息（包含最新的字段列表）
     * @return 是否更新成功
     * @throws Exception 更新失败时抛出异常
     */
    boolean updateDtoFile(String filePath, ClassInfo newClassInfo) throws Exception;

    /**
     * 更新VO文件 - 重新生成VO文件（因为VO继承自DTO）
     * 
     * @param filePath VO文件路径
     * @param newClassInfo 新的类信息
     * @return 是否更新成功
     * @throws Exception 更新失败时抛出异常
     */
    boolean updateVoFile(String filePath, ClassInfo newClassInfo) throws Exception;

    /**
     * 更新Mapper.xml文件 - 更新resultMap、insert、update语句
     * 
     * @param filePath Mapper.xml文件路径
     * @param newClassInfo 新的类信息
     * @return 是否更新成功
     * @throws Exception 更新失败时抛出异常
     */
    boolean updateMapperXmlFile(String filePath, ClassInfo newClassInfo) throws Exception;
}
