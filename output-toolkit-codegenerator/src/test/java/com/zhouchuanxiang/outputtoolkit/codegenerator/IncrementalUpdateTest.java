package com.zhouchuanxiang.outputtoolkit.codegenerator;

import com.zhouchuanxiang.outputtoolkit.codegenerator.entity.FieldInfo;
import com.zhouchuanxiang.outputtoolkit.codegenerator.util.FieldDiffUtil;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Author zhouchuanxiang
 * @Description 增量更新功能测试
 * @Date 2026-02-04
 */
public class IncrementalUpdateTest {

    @Test
    public void testFieldDiffUtil_addFields() {
        // 模拟旧字段列表
        List<FieldInfo> oldFields = new ArrayList<>();
        FieldInfo field1 = new FieldInfo();
        field1.setFieldName("userName");
        field1.setFieldClass("String");
        oldFields.add(field1);

        // 模拟新字段列表（增加了age字段）
        List<FieldInfo> newFields = new ArrayList<>();
        newFields.add(field1);
        
        FieldInfo field2 = new FieldInfo();
        field2.setFieldName("age");
        field2.setFieldClass("Integer");
        newFields.add(field2);

        // 执行比对
        FieldDiffUtil.FieldDiff diff = FieldDiffUtil.compareFields(oldFields, newFields);

        // 验证结果
        assertTrue(diff.hasChanges(), "应该检测到变更");
        assertEquals(1, diff.getAddedFields().size(), "应该有1个新增字段");
        assertEquals("age", diff.getAddedFields().get(0).getFieldName(), "新增字段应该是age");
        assertEquals(0, diff.getRemovedFields().size(), "不应该有删除的字段");
        assertEquals(1, diff.getRemainingFields().size(), "应该有1个保留字段");
    }

    @Test
    public void testFieldDiffUtil_removeFields() {
        // 模拟旧字段列表
        List<FieldInfo> oldFields = new ArrayList<>();
        FieldInfo field1 = new FieldInfo();
        field1.setFieldName("userName");
        field1.setFieldClass("String");
        oldFields.add(field1);
        
        FieldInfo field2 = new FieldInfo();
        field2.setFieldName("age");
        field2.setFieldClass("Integer");
        oldFields.add(field2);

        // 模拟新字段列表（删除了age字段）
        List<FieldInfo> newFields = new ArrayList<>();
        newFields.add(field1);

        // 执行比对
        FieldDiffUtil.FieldDiff diff = FieldDiffUtil.compareFields(oldFields, newFields);

        // 验证结果
        assertTrue(diff.hasChanges(), "应该检测到变更");
        assertEquals(0, diff.getAddedFields().size(), "不应该有新增字段");
        assertEquals(1, diff.getRemovedFields().size(), "应该有1个删除的字段");
        assertEquals("age", diff.getRemovedFields().get(0).getFieldName(), "删除的字段应该是age");
        assertEquals(1, diff.getRemainingFields().size(), "应该有1个保留字段");
    }

    @Test
    public void testFieldDiffUtil_noChanges() {
        // 模拟相同的字段列表
        List<FieldInfo> oldFields = new ArrayList<>();
        FieldInfo field1 = new FieldInfo();
        field1.setFieldName("userName");
        field1.setFieldClass("String");
        oldFields.add(field1);

        List<FieldInfo> newFields = new ArrayList<>();
        newFields.add(field1);

        // 执行比对
        FieldDiffUtil.FieldDiff diff = FieldDiffUtil.compareFields(oldFields, newFields);

        // 验证结果
        assertFalse(diff.hasChanges(), "不应该检测到变更");
        assertEquals(0, diff.getAddedFields().size(), "不应该有新增字段");
        assertEquals(0, diff.getRemovedFields().size(), "不应该有删除字段");
        assertEquals(1, diff.getRemainingFields().size(), "应该有1个保留字段");
    }

    @Test
    public void testFieldDiffUtil_excludeFields() {
        // 创建字段列表
        List<FieldInfo> fields = new ArrayList<>();
        
        FieldInfo field1 = new FieldInfo();
        field1.setFieldName("id");
        fields.add(field1);
        
        FieldInfo field2 = new FieldInfo();
        field2.setFieldName("userName");
        fields.add(field2);
        
        FieldInfo field3 = new FieldInfo();
        field3.setFieldName("createdAt");
        fields.add(field3);

        // 排除基类字段
        List<String> excludeList = new ArrayList<>();
        excludeList.add("id");
        excludeList.add("createdAt");
        
        List<FieldInfo> filtered = FieldDiffUtil.excludeFields(fields, excludeList);

        // 验证结果
        assertEquals(1, filtered.size(), "应该只剩1个字段");
        assertEquals("userName", filtered.get(0).getFieldName(), "保留的字段应该是userName");
    }

    @Test
    public void testFieldDiffUtil_addAndRemove() {
        // 模拟旧字段列表
        List<FieldInfo> oldFields = new ArrayList<>();
        FieldInfo field1 = new FieldInfo();
        field1.setFieldName("userName");
        oldFields.add(field1);
        
        FieldInfo field2 = new FieldInfo();
        field2.setFieldName("email");
        oldFields.add(field2);

        // 模拟新字段列表（删除email，添加phone和age）
        List<FieldInfo> newFields = new ArrayList<>();
        newFields.add(field1);
        
        FieldInfo field3 = new FieldInfo();
        field3.setFieldName("phone");
        newFields.add(field3);
        
        FieldInfo field4 = new FieldInfo();
        field4.setFieldName("age");
        newFields.add(field4);

        // 执行比对
        FieldDiffUtil.FieldDiff diff = FieldDiffUtil.compareFields(oldFields, newFields);

        // 验证结果
        assertTrue(diff.hasChanges(), "应该检测到变更");
        assertEquals(2, diff.getAddedFields().size(), "应该有2个新增字段");
        assertEquals(1, diff.getRemovedFields().size(), "应该有1个删除字段");
        assertEquals("email", diff.getRemovedFields().get(0).getFieldName(), "删除的应该是email");
        assertEquals(1, diff.getRemainingFields().size(), "应该有1个保留字段");
        assertEquals(3, diff.getAllNewFields().size(), "新字段列表应该有3个");
    }
}
