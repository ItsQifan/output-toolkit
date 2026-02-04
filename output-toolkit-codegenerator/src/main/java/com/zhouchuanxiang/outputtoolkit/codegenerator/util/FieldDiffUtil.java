package com.zhouchuanxiang.outputtoolkit.codegenerator.util;

import com.zhouchuanxiang.outputtoolkit.codegenerator.entity.FieldInfo;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author zhouchuanxiang
 * @Description 字段差异比对工具类
 * @Date 2026-02-04
 */
public class FieldDiffUtil {

    /**
     * 字段差异结果
     */
    @Data
    public static class FieldDiff {
        /**
         * 新增的字段列表
         */
        private List<FieldInfo> addedFields = new ArrayList<>();
        
        /**
         * 删除的字段列表（只在旧字段中存在）
         */
        private List<FieldInfo> removedFields = new ArrayList<>();
        
        /**
         * 保留的字段列表（新旧都存在）
         */
        private List<FieldInfo> remainingFields = new ArrayList<>();
        
        /**
         * 所有新字段列表（包含保留+新增）
         */
        private List<FieldInfo> allNewFields = new ArrayList<>();

        /**
         * 是否有变更
         */
        public boolean hasChanges() {
            return !addedFields.isEmpty() || !removedFields.isEmpty();
        }
    }

    /**
     * 比较新旧字段列表
     * 
     * @param oldFields 旧字段列表
     * @param newFields 新字段列表
     * @return 字段差异结果
     */
    public static FieldDiff compareFields(List<FieldInfo> oldFields, List<FieldInfo> newFields) {
        FieldDiff diff = new FieldDiff();
        
        if (oldFields == null) {
            oldFields = new ArrayList<>();
        }
        if (newFields == null) {
            newFields = new ArrayList<>();
        }

        // 创建字段名映射
        Map<String, FieldInfo> oldFieldMap = oldFields.stream()
                .collect(Collectors.toMap(FieldInfo::getFieldName, f -> f));
        
        Map<String, FieldInfo> newFieldMap = newFields.stream()
                .collect(Collectors.toMap(FieldInfo::getFieldName, f -> f));

        // 找出新增的字段
        for (FieldInfo newField : newFields) {
            if (!oldFieldMap.containsKey(newField.getFieldName())) {
                diff.getAddedFields().add(newField);
            } else {
                diff.getRemainingFields().add(newField);
            }
        }

        // 找出删除的字段
        for (FieldInfo oldField : oldFields) {
            if (!newFieldMap.containsKey(oldField.getFieldName())) {
                diff.getRemovedFields().add(oldField);
            }
        }

        // 所有新字段列表 = 保留 + 新增
        diff.setAllNewFields(newFields);

        return diff;
    }

    /**
     * 从字段列表中排除指定的字段名
     * 
     * @param fields 字段列表
     * @param excludeFieldNames 要排除的字段名列表
     * @return 过滤后的字段列表
     */
    public static List<FieldInfo> excludeFields(List<FieldInfo> fields, List<String> excludeFieldNames) {
        if (fields == null || excludeFieldNames == null || excludeFieldNames.isEmpty()) {
            return fields;
        }
        
        Set<String> excludeSet = new HashSet<>(excludeFieldNames);
        return fields.stream()
                .filter(field -> !excludeSet.contains(field.getFieldName()))
                .collect(Collectors.toList());
    }
}
