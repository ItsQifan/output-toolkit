<?xml version="1.0" encoding="UTF-8"?>
<config>
    <!-- <appName>MyApplication</appName>
     <version>1.0.0</version>-->

    <tableConfigList>
        <#list tableConfigs as tableConfig>
            <tableConfig>
                <!--开关-->
                <genSwitch>true</genSwitch>
                <!--模式：sql-根据建表语句生成代码；   -->
                <model>sql</model>
                <authorName>zhouchuanxiang</authorName>

                <!--SQL建表语句-->
                <sql>
                    ${tableConfig.sql}
                </sql>
                <!--建表语句表名忽略的前缀-->
                <sqlIgnorePrefix>${tableConfig.sqlIgnorePrefix}</sqlIgnorePrefix>

                <!-- ================== 后缀配置 ================== -->
                <!--dto的后缀，如DTO还是DO等-->
                <dtoSuffix>${tableConfig.dtoSuffix}</dtoSuffix>
                <!--mapper的后缀，如Mapper还是Dao等-->
                <mapperSuffix>${tableConfig.mapperSuffix}</mapperSuffix>
                <!--manager的后缀，如Manager还是Service等-->
                <managerSuffix>${tableConfig.managerSuffix}</managerSuffix>

                <!-- ================== 包名配置 ================== -->
                <!--dto所在包名-->
                <dtoPackageName>${tableConfig.dtoPackageName}</dtoPackageName>
                <!--vo所在包名-->
                <voPackageName>${tableConfig.voPackageName}</voPackageName>
                <!--mapper所在包名-->
                <mapperPackageName>${tableConfig.mapperPackageName}</mapperPackageName>
                <!--service/manager所在包名-->
                <managerPackageName>${tableConfig.managerPackageName}</managerPackageName>
                <!--controller所在包名-->
                <controllerPackageName>${tableConfig.controllerPackageName}</controllerPackageName>

                <!-- ================== 输出本地路径配置 ================== -->
                <dtoLocalPath></dtoLocalPath>
                <voLocalPath></voLocalPath>
                <mapperLocalPath></mapperLocalPath>
                <managerLocalPath></managerLocalPath>
                <controllerLocalPath></controllerLocalPath>
            </tableConfig>
        </#list>
    </tableConfigList>
</config>