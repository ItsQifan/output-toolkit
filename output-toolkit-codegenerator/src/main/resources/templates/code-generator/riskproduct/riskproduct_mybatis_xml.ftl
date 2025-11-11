<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="${mapperPackageName}.${classInfo.className}${mapperSuffix}">

   <#-- <resultMap id="BaseResultMap" type="${classInfo.dtoPackageName}" >
        <#if classInfo.fieldList?exists && classInfo.fieldList?size gt 0>
            <#list classInfo.fieldList as fieldItem>
                <result column="${fieldItem.columnName?right_pad(20)}" property="${fieldItem.fieldName?right_pad(20)}" jdbcType="${fieldItem.fieldXmlClass?right_pad(10)}" />
            </#list>
        </#if>
    </resultMap>-->

    <resultMap id="BaseResultMap" type="${classInfo.dtoPackageName}">
        <#if classInfo.fieldList?exists && classInfo.fieldList?size gt 0>
            <#assign maxColumnLen = classInfo.fieldList?map(field -> field.columnName?length)?max>
            <#assign maxPropertyLen = classInfo.fieldList?map(field -> field.fieldName?length)?max>

            <#list classInfo.fieldList as fieldItem>
                <result column="${fieldItem.columnName}"<#rt>
                        <#lt><#list 1..(maxColumnLen - fieldItem.columnName?length + 4) as i> </#list>property="${fieldItem.fieldName}"<#rt>
                        <#lt><#list 1..(maxPropertyLen - fieldItem.fieldName?length + 4) as i> </#list>jdbcType="${fieldItem.fieldXmlClass}" />
            </#list>
        </#if>
    </resultMap>


    <sql id="Base_Column_List">
        <#if classInfo.fieldList?exists && classInfo.fieldList?size gt 0>
            <#list classInfo.fieldList as fieldItem >
                ${fieldItem.columnName}<#if fieldItem_has_next>,</#if>
            </#list>
        </#if>
    </sql>

    <insert id="insert" useGeneratedKeys="true" keyColumn="id" keyProperty="id" parameterType="${classInfo.dtoPackageName}">
        INSERT INTO ${classInfo.originTableName}
        <trim prefix="(" suffix=")" suffixOverrides=",">
        <#if classInfo.fieldList?exists && classInfo.fieldList?size gt 0>
            <#list classInfo.fieldList as fieldItem >
                <#if fieldItem.columnName != "id" >
                <if test="${fieldItem.fieldName}!= null <#if fieldItem.fieldClass ="String">and  ${fieldItem.fieldName} !='' </#if>">
                    ${fieldItem.columnName}<#if fieldItem_has_next>,</#if>
                ${r"</if>"}
                </#if>
            </#list>
        </#if>
        </trim>
        <trim prefix="values (" suffix=")" suffixOverrides=",">
        <#if classInfo.fieldList?exists && classInfo.fieldList?size gt 0>
            <#list classInfo.fieldList as fieldItem >
                <#if fieldItem.columnName != "id" >
                <#--<#if fieldItem.columnName="addtime" || fieldItem.columnName="updatetime" >
                ${r"<if test ='null != "}${fieldItem.fieldName}${r"'>"}
                    NOW()<#if fieldItem_has_next>,</#if>
                ${r"</if>"}
                <#else>-->
                <if test="${fieldItem.fieldName}!=null <#if fieldItem.fieldClass ="String">and  ${fieldItem.fieldName} !='' </#if>">
                    ${r"#{"}${fieldItem.fieldName}${r"}"}<#if fieldItem_has_next>,</#if>
                ${r"</if>"}
                <#--</#if>-->
                </#if>
            </#list>
        </#if>
        </trim>
    </insert>

    <delete id="delete" >
        DELETE FROM ${classInfo.originTableName}
        WHERE id = ${r"#{id}"}
    </delete>

    <update id="update" parameterType="${classInfo.dtoPackageName}">
        UPDATE ${classInfo.originTableName}
        <set>
        <#list classInfo.fieldList as fieldItem >
            <#if fieldItem.columnName != "id" && fieldItem.columnName != "AddTime" && fieldItem.columnName != "UpdateTime" >
                <if test=" ${fieldItem.fieldName}!= null  <#if fieldItem.fieldClass ="String">and  ${fieldItem.fieldName} !='' </#if>">
                    ${fieldItem.columnName} = ${r"#{"}${fieldItem.fieldName}${r"}"}<#if fieldItem_has_next>,</#if>
                ${r"</if>"}
            </#if>
        </#list>
        </set>
        WHERE id = ${r"#{"}id${r"}"}
    </update>


    <select id="queryById" resultMap="BaseResultMap">
        SELECT
        <include refid="Base_Column_List" />
        FROM ${classInfo.originTableName}
        WHERE id = ${r"#{id}"}
    </select>

    <select id="pageList" resultMap="BaseResultMap">
        SELECT
        <include refid="Base_Column_List" />
        FROM ${classInfo.originTableName}
        <where>
            <#if classInfo.fieldList?exists && classInfo.fieldList?size gt 0>
            <#list classInfo.fieldList as fieldItem >
            <#if fieldItem.columnName != "id" >
            <if test="${fieldItem.fieldName}!= null <#if fieldItem.fieldClass ="String">and  ${fieldItem.fieldName} !='' </#if>">
                and ${fieldItem.columnName} = ${r"#{"}${fieldItem.fieldName}${r",jdbcType="}${fieldItem.fieldXmlClass}${r"}"}
            ${r"</if>"}
            </#if>
            </#list>
            </#if>
        </where>
    </select>


</mapper>