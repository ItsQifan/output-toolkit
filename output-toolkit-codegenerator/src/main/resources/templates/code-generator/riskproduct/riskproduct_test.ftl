<#if controllerPackageName??>
package ${controllerPackageName}.${classInfo.className}Controller;

</#if>
import com.alibaba.fastjson2.JSON;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.mockito.Mockito;
import com.bestpay.riskcontrol.riskproduct.api.utils.RequestMsgUtil;
import com.bestpay.riskcontrol.riskproduct.api.vo.RequestMsgVO;
import java.util.Date;
import java.math.BigDecimal;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import ${classInfo.voPackageName}.$${classInfo.className}VO;

/**
 * ============================================================
 * 测试类使用技巧说明
 * ============================================================
 * 
 * ● @MockBean 注解可以模拟注入项目需要但测试类不需要的类
 *   示例: @MockBean private SomeService someService;
 *   适用场景: 隔离测试、外部服务依赖、降低测试复杂度
 * 
 * ● 使用 @Before 注解的方法在每个测试方法执行前都会运行
 *   适用场景: 初始化测试数据、设置公共配置、Mock 返回值
 * 
 * ● 配置文件技巧 (application.yml 或 application-test.yml):
 *   - 忽略 Zookeeper: dubbo.registry.address=N/A 或 dubbo.zk.url=none
 *   - 忽略 Redis: spring.redis.host=127.0.0.1 (使用本地或不启动)
 *   - 使用内存数据库: spring.datasource.url=jdbc:h2:mem:testdb
 *   - 禁用定时任务: spring.task.scheduling.enabled=false
 *   - 禁用消息队列: spring.rabbitmq.listener.auto-startup=false
 * 
 * ● 使用 @TestPropertySource 可以覆盖配置
 *   示例: @TestPropertySource(properties = {"dubbo.registry.address=N/A"})
 * 
 * ● Mockito 常用方法:
 *   - when(mock.method()).thenReturn(value) : 模拟方法返回值
 *   - when(mock.method()).thenThrow(exception) : 模拟抛出异常
 *   - verify(mock).method() : 验证方法是否被调用
 *   - doNothing().when(mock).method() : 模拟 void 方法
 * 
 * ● 事务回滚技巧:
 *   添加 @Transactional 和 @Rollback(true) 可以让测试完成后自动回滚数据
 *   避免测试数据污染数据库
 * 
 * ● 使用 @Sql 注解可以在测试前执行 SQL 脚本
 *   示例: @Sql(scripts = "/test-data.sql")
 * 
 * ● 如果测试需要完整的 Spring 上下文，使用 @SpringBootTest
 *   如果只测试某一层，可以使用:
 *   - @WebMvcTest : 只加载 Web 层
 *   - @DataJpaTest : 只加载 JPA 相关
 *   - @JsonTest : 只测试 JSON 序列化
 * 
 * ● 参数化测试可以使用 @Parameterized 减少重复代码
 * 
 * ● 使用 @Ignore 可以临时忽略某个测试方法
 * 
 * ============================================================
 * 
 * @description ${classInfo.classComment} Controller 测试类
 * @author ${authorName}
 * @date ${.now?string('yyyy-MM-dd')}
 * @fileName ${classInfo.className}ControllerTest
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
// @TestPropertySource(properties = {"dubbo.registry.address=N/A"}) // 如需忽略 Dubbo 注册中心，取消此注释
// @Transactional // 如需测试后自动回滚数据，取消此注释
// @Rollback(true) // 配合 @Transactional 使用，确保回滚
public class ${classInfo.className}ControllerTest {

    @Autowired
    private ${classInfo.className}Controller ${classInfo.className?uncap_first}Controller;

    // ============================================================
    // Mock 示例：如果需要隔离测试某些依赖服务，可以使用 @MockBean
    // ============================================================
    // @MockBean
    // private SomeExternalService someExternalService;
    
    // @MockBean
    // private ${classInfo.className}${managerSuffix} ${classInfo.className?uncap_first}${managerSuffix};


    /**
     * 测试前置方法
     * 在每个测试方法执行前都会运行
     * 用于初始化测试数据、设置 Mock 返回值等
     */
    @Before
    public void setUp() throws Exception {
        log.info("========== 测试前置准备开始 ==========");
        
        // 设置用户信息到 RequestMsgUtil（如果项目使用了 ThreadLocal 存储用户信息）
        RequestMsgVO requestMsgVO = new RequestMsgVO();
        requestMsgVO.setUserName("testUser");
        RequestMsgUtil.add(requestMsgVO);
        
        // ============================================================
        // Mock 示例：模拟外部服务的返回值
        // ============================================================
        // 示例1: 模拟查询方法返回固定值
        // ${classInfo.className}VO mockVO = new ${classInfo.className}VO();
        // mockVO.setId("001");
        // when(${classInfo.className?uncap_first}${managerSuffix}.queryById(any())).thenReturn(mockVO);
        
        // 示例2: 模拟方法返回成功
        // when(${classInfo.className?uncap_first}${managerSuffix}.insert(any())).thenReturn(true);
        
        // 示例3: 模拟外部服务
        // when(someExternalService.callRemoteApi(anyString())).thenReturn("mock response");
        
        // 示例4: 模拟抛出异常
        // when(someService.method()).thenThrow(new RuntimeException("模拟异常"));
        
        // 示例5: 模拟 void 方法
        // doNothing().when(someService).voidMethod(any());
        
        log.info("========== 测试前置准备完成 ==========");
    }
    
    // ============================================================
    // 如果需要在所有测试方法执行后清理资源，可以使用 @After
    // ============================================================
    // @After
    // public void tearDown() {
    //     log.info("========== 测试后置清理开始 ==========");
    //     // 清理测试数据
    //     // 关闭资源
    //     log.info("========== 测试后置清理完成 ==========");
    // }


    /**
     * 测试新增方法
     * @author ${authorName}
     * @date ${.now?string('yyyy/MM/dd')}
     * 
     * 测试技巧:
     * 1. 如果使用了 @MockBean，可以在此方法内单独设置 Mock 行为
     * 2. 使用 @Test(expected = Exception.class) 可以测试预期抛出异常的场景
     * 3. 使用 @Test(timeout = 1000) 可以限制测试方法的执行时间（毫秒）
     */
    @Test
    public void testInsert() {
        log.info("========== 开始测试 ${classInfo.className}Controller.insert 方法 ==========");
        
        // ============================================================
        // 如果使用 MockBean，可以在这里设置特定的 Mock 行为
        // ============================================================
        // when(${classInfo.className?uncap_first}${managerSuffix}.insert(any())).thenReturn(true);
        
        // 构造测试数据
        ${classInfo.className}VO ${classInfo.className?uncap_first} = new ${classInfo.className}VO();
<#if classInfo.fieldList?exists && classInfo.fieldList?size gt 0>
    <#-- 定义要排除的字段列表（这些字段由BaseDTO或数据库自动生成） -->
    <#assign excludeFields = ["id", "createdAt", "createdBy", "updatedAt", "updatedBy"]>
    
    <#list classInfo.fieldList as fieldItem>
        <#if !excludeFields?seq_contains(fieldItem.fieldName)>
        // ${fieldItem.fieldComment}
        ${classInfo.className?uncap_first}.set${fieldItem.fieldName?cap_first}(<#if fieldItem.fieldClass == "String">"test_${fieldItem.fieldName}"<#elseif fieldItem.fieldClass == "Integer">1<#elseif fieldItem.fieldClass == "Long">"001"<#elseif fieldItem.fieldClass == "Date">new Date()<#elseif fieldItem.fieldClass == "Boolean">true<#elseif fieldItem.fieldClass == "BigDecimal">new BigDecimal("100.00")<#elseif fieldItem.fieldClass == "Double">100.0<#elseif fieldItem.fieldClass == "Float">100.0f<#elseif fieldItem.fieldClass == "Short">(short)1<#elseif fieldItem.fieldClass == "Byte">(byte)1<#else>null</#if>);
        </#if>
    </#list>
</#if>
        
        // 打印入参
        log.info("入参 ${classInfo.className?uncap_first}={}", JSON.toJSONString(${classInfo.className?uncap_first}));
        
        // 调用方法
        Boolean result = ${classInfo.className?uncap_first}Controller.insert(${classInfo.className?uncap_first});
        
        // 打印返回值
        log.info("返回值 result={}", result);
        
        // ============================================================
        // 断言验证
        // ============================================================
        assertNotNull("返回结果不能为空", result);
        assertTrue("新增操作应该成功", result);
        
        // 其他断言示例:
        // assertEquals("期望值", "实际值"); // 验证两个值相等
        // assertFalse("条件应该为假", condition); // 验证条件为假
        // assertNull("对象应该为空", object); // 验证对象为空
        // assertSame(expected, actual); // 验证两个对象引用相同
        // assertNotSame(expected, actual); // 验证两个对象引用不同
        // assertArrayEquals(expectedArray, actualArray); // 验证数组相等
        
        // ============================================================
        // 如果使用了 MockBean，可以验证 Mock 方法是否被调用
        // ============================================================
        // verify(${classInfo.className?uncap_first}${managerSuffix}).insert(any()); // 验证方法被调用
        // verify(${classInfo.className?uncap_first}${managerSuffix}, times(1)).insert(any()); // 验证方法被调用1次
        // verify(${classInfo.className?uncap_first}${managerSuffix}, never()).delete(any()); // 验证方法从未被调用
        // verify(${classInfo.className?uncap_first}${managerSuffix}, atLeast(1)).insert(any()); // 验证至少被调用1次
        
        log.info("========== 测试 ${classInfo.className}Controller.insert 方法结束 ==========");
    }

    /**
     * 测试删除方法
     * @author ${authorName}
     * @date ${.now?string('yyyy/MM/dd')}
     */
    @Test
    public void testDelete() {
        log.info("========== 开始测试 ${classInfo.className}Controller.delete 方法 ==========");
        
        // 构造测试数据
        ${classInfo.className}VO ${classInfo.className?uncap_first} = new ${classInfo.className}VO();
        ${classInfo.className?uncap_first}.setId("001"); // 设置要删除的记录ID
        
        // 打印入参
        log.info("入参 ${classInfo.className?uncap_first}={}", JSON.toJSONString(${classInfo.className?uncap_first}));
        
        // 调用方法
        Boolean result = ${classInfo.className?uncap_first}Controller.delete(${classInfo.className?uncap_first});
        
        // 打印返回值
        log.info("返回值 result={}", result);
        
        // 断言验证
        assertNotNull("返回结果不能为空", result);
        
        log.info("========== 测试 ${classInfo.className}Controller.delete 方法结束 ==========");
    }

    /**
     * 测试更新方法
     * @author ${authorName}
     * @date ${.now?string('yyyy/MM/dd')}
     */
    @Test
    public void testUpdate() {
        log.info("========== 开始测试 ${classInfo.className}Controller.update 方法 ==========");
        
        // 构造测试数据
        ${classInfo.className}VO ${classInfo.className?uncap_first} = new ${classInfo.className}VO();
        ${classInfo.className?uncap_first}.setId("001"); // 设置要更新的记录ID
<#if classInfo.fieldList?exists && classInfo.fieldList?size gt 0>
    <#assign excludeFields = ["id", "createdAt", "createdBy", "updatedAt", "updatedBy"]>
    
    <#list classInfo.fieldList as fieldItem>
        <#if !excludeFields?seq_contains(fieldItem.fieldName)>
        // ${fieldItem.fieldComment}
        ${classInfo.className?uncap_first}.set${fieldItem.fieldName?cap_first}(<#if fieldItem.fieldClass == "String">"updated_${fieldItem.fieldName}"<#elseif fieldItem.fieldClass == "Integer">2<#elseif fieldItem.fieldClass == "Long">2L<#elseif fieldItem.fieldClass == "Date">new Date()<#elseif fieldItem.fieldClass == "Boolean">false<#elseif fieldItem.fieldClass == "BigDecimal">new BigDecimal("200.00")<#elseif fieldItem.fieldClass == "Double">200.0<#elseif fieldItem.fieldClass == "Float">200.0f<#elseif fieldItem.fieldClass == "Short">(short)2<#elseif fieldItem.fieldClass == "Byte">(byte)2<#else>null</#if>);
        </#if>
    </#list>
</#if>
        
        // 打印入参
        log.info("入参 ${classInfo.className?uncap_first}={}", JSON.toJSONString(${classInfo.className?uncap_first}));
        
        // 调用方法
        Boolean result = ${classInfo.className?uncap_first}Controller.update(${classInfo.className?uncap_first});
        
        // 打印返回值
        log.info("返回值 result={}", result);
        
        // 断言验证
        assertNotNull("返回结果不能为空", result);
        assertTrue("更新操作应该成功", result);
        
        log.info("========== 测试 ${classInfo.className}Controller.update 方法结束 ==========");
    }

    /**
     * 测试根据ID查询方法
     * @author ${authorName}
     * @date ${.now?string('yyyy/MM/dd')}
     */
    @Test
    public void testQueryById() {
        log.info("========== 开始测试 ${classInfo.className}Controller.queryById 方法 ==========");
        
        // 构造测试数据
        ${classInfo.className}VO ${classInfo.className?uncap_first} = new ${classInfo.className}VO();
        ${classInfo.className?uncap_first}.setId("001"); // 设置要查询的记录ID
        
        // 打印入参
        log.info("入参 ${classInfo.className?uncap_first}={}", JSON.toJSONString(${classInfo.className?uncap_first}));
        
        // 调用方法
        ${classInfo.className}VO result = ${classInfo.className?uncap_first}Controller.queryById(${classInfo.className?uncap_first});
        
        // 打印返回值
        log.info("返回值 result={}", JSON.toJSONString(result));
        
        // 断言验证
        assertNotNull("查询结果不能为空", result);
        assertNotNull("查询结果的ID不能为空", result.getId());
        
        log.info("========== 测试 ${classInfo.className}Controller.queryById 方法结束 ==========");
    }

    /**
     * 测试分页查询方法
     * @author ${authorName}
     * @date ${.now?string('yyyy/MM/dd')}
     */
    @Test
    public void testPageList() {
        log.info("========== 开始测试 ${classInfo.className}Controller.pageList 方法 ==========");
        
        // 构造测试数据
        ${classInfo.className}VO ${classInfo.className?uncap_first} = new ${classInfo.className}VO();
        ${classInfo.className?uncap_first}.setPageStart(1); // 起始页
        ${classInfo.className?uncap_first}.setPageSize(10); // 每页大小
<#if classInfo.fieldList?exists && classInfo.fieldList?size gt 0>
    <#-- 可以设置查询条件 -->
    <#assign excludeFields = ["id", "createdAt", "createdBy", "updatedAt", "updatedBy"]>
    
    <#list classInfo.fieldList as fieldItem>
        <#if !excludeFields?seq_contains(fieldItem.fieldName)>
        // 查询条件: ${fieldItem.fieldComment}
        // ${classInfo.className?uncap_first}.set${fieldItem.fieldName?cap_first}(<#if fieldItem.fieldClass == "String">"test"<#elseif fieldItem.fieldClass == "Integer">1<#elseif fieldItem.fieldClass == "Long">"001"<#else>null</#if>);
        </#if>
    </#list>
</#if>
        
        // 打印入参
        log.info("入参 ${classInfo.className?uncap_first}={}", JSON.toJSONString(${classInfo.className?uncap_first}));
        
        // 调用方法
        PageInfo<${classInfo.className}VO> pageInfo = ${classInfo.className?uncap_first}Controller.pageList(${classInfo.className?uncap_first});
        
        // 打印返回值
        log.info("返回值 pageInfo={}", JSON.toJSONString(pageInfo));
        log.info("总记录数={}, 总页数={}, 当前页={}, 每页大小={}", 
                pageInfo.getTotal(), pageInfo.getPages(), pageInfo.getPageNum(), pageInfo.getPageSize());
        
        // 断言验证
        assertNotNull("分页查询结果不能为空", pageInfo);
        assertNotNull("分页查询列表不能为空", pageInfo.getList());
        
        log.info("========== 测试 ${classInfo.className}Controller.pageList 方法结束 ==========");
    }

    // ============================================================
    // 异常场景测试示例（可选）
    // ============================================================
    
    /**
     * 测试新增方法 - 参数为空的异常场景
     * @author ${authorName}
     * @date ${.now?string('yyyy/MM/dd')}
     * 
     * 使用 @Test(expected = Exception.class) 测试预期会抛出异常的场景
     */
    // @Test(expected = IllegalArgumentException.class)
    // public void testInsert_WithNullParam() {
    //     log.info("========== 开始测试 ${classInfo.className}Controller.insert 方法 - 参数为空异常场景 ==========");
    //     
    //     // 传入空参数，应该抛出异常
    //     ${classInfo.className?uncap_first}Controller.insert(null);
    //     
    //     log.info("========== 测试 ${classInfo.className}Controller.insert 方法 - 参数为空异常场景结束 ==========");
    // }
    
    /**
     * 测试更新方法 - ID 不存在的场景
     * @author ${authorName}
     * @date ${.now?string('yyyy/MM/dd')}
     */
    // @Test
    // public void testUpdate_WithNonExistentId() {
    //     log.info("========== 开始测试 ${classInfo.className}Controller.update 方法 - ID不存在场景 ==========");
    //     
    //     // 使用不存在的 ID
    //     ${classInfo.className}VO ${classInfo.className?uncap_first} = new ${classInfo.className}VO();
    //     ${classInfo.className?uncap_first}.setId(999999L); // 不存在的ID
    //     
    //     log.info("入参 ${classInfo.className?uncap_first}={}", JSON.toJSONString(${classInfo.className?uncap_first}));
    //     
    //     // 使用 MockBean 模拟返回 false
    //     // when(${classInfo.className?uncap_first}${managerSuffix}.update(any())).thenReturn(false);
    //     
    //     Boolean result = ${classInfo.className?uncap_first}Controller.update(${classInfo.className?uncap_first});
    //     
    //     log.info("返回值 result={}", result);
    //     
    //     // 验证更新失败
    //     assertFalse("ID不存在时更新应该失败", result);
    //     
    //     log.info("========== 测试 ${classInfo.className}Controller.update 方法 - ID不存在场景结束 ==========");
    // }
    
    /**
     * 测试分页查询 - 边界值测试
     * @author ${authorName}
     * @date ${.now?string('yyyy/MM/dd')}
     */
    // @Test
    // public void testPageList_BoundaryValue() {
    //     log.info("========== 开始测试 ${classInfo.className}Controller.pageList 方法 - 边界值测试 ==========");
    //     
    //     // 测试边界值：pageSize = 0
    //     ${classInfo.className}VO ${classInfo.className?uncap_first} = new ${classInfo.className}VO();
    //     ${classInfo.className?uncap_first}.setPageStart(1);
    //     ${classInfo.className?uncap_first}.setPageSize(0);
    //     
    //     log.info("入参 ${classInfo.className?uncap_first}={}", JSON.toJSONString(${classInfo.className?uncap_first}));
    //     
    //     PageInfo<${classInfo.className}VO> pageInfo = ${classInfo.className?uncap_first}Controller.pageList(${classInfo.className?uncap_first});
    //     
    //     log.info("返回值 pageInfo={}", JSON.toJSONString(pageInfo));
    //     
    //     // 根据业务逻辑验证结果
    //     assertNotNull("分页查询结果不能为空", pageInfo);
    //     
    //     log.info("========== 测试 ${classInfo.className}Controller.pageList 方法 - 边界值测试结束 ==========");
    // }
}
