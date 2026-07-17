package com.github.houbb.core.audit.application.diff;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.houbb.core.audit.application.domain.diff.Change;
import com.github.houbb.core.audit.application.domain.diff.ChangeSet;
import com.github.houbb.core.audit.application.domain.enums.ChangeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Java Bean 字段级比较策略
 * <p>使用反射遍历对象字段，跳过 @AuditIgnore 标记的字段。</p>
 * <p>支持 @AuditDiffField 自定义字段名。</p>
 * <p>嵌套对象递归比较为 JSON 路径。</p>
 */
@Component
public class BeanDiffStrategy implements DiffStrategy<Object> {

    private static final Logger log = LoggerFactory.getLogger(BeanDiffStrategy.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(Class<?> type) {
        if (type == null) return false;
        if (type.isPrimitive()) return false;
        if (type.getName().startsWith("java.lang.")) return false;
        if (Collection.class.isAssignableFrom(type)) return false;
        if (Map.class.isAssignableFrom(type)) return false;
        return true;
    }

    @Override
    public ChangeSet compare(Object before, Object after) {
        List<Change> changes = new ArrayList<>();

        if (before == null && after == null) {
            return ChangeSet.builder().changes(changes).build();
        }

        Set<String> allFields = new LinkedHashSet<>();
        Map<String, Object> beforeFieldValues = new HashMap<>();
        Map<String, Object> afterFieldValues = new HashMap<>();

        if (before != null) {
            collectFieldValues(before, beforeFieldValues, "");
            beforeFieldValues.keySet().forEach(f -> allFields.add(f));
        }
        if (after != null) {
            collectFieldValues(after, afterFieldValues, "");
            afterFieldValues.keySet().forEach(f -> allFields.add(f));
        }

        for (String fieldName : allFields) {
            Object beforeVal = beforeFieldValues.get(fieldName);
            Object afterVal = afterFieldValues.get(fieldName);

            Change change;
            if (beforeVal == null && afterVal == null) {
                continue; // skip unchanged nulls
            } else if (beforeVal == null && afterVal != null) {
                change = Change.builder()
                        .fieldName(fieldName)
                        .changeType(ChangeType.ADD)
                        .beforeValue(null)
                        .afterValue(toDisplayString(afterVal))
                        .build();
            } else if (beforeVal != null && afterVal == null) {
                change = Change.builder()
                        .fieldName(fieldName)
                        .changeType(ChangeType.REMOVE)
                        .beforeValue(toDisplayString(beforeVal))
                        .afterValue(null)
                        .build();
            } else if (Objects.equals(beforeVal, afterVal)) {
                change = Change.builder()
                        .fieldName(fieldName)
                        .changeType(ChangeType.UNCHANGED)
                        .beforeValue(toDisplayString(beforeVal))
                        .afterValue(toDisplayString(afterVal))
                        .build();
            } else {
                change = Change.builder()
                        .fieldName(fieldName)
                        .changeType(ChangeType.UPDATE)
                        .beforeValue(toDisplayString(beforeVal))
                        .afterValue(toDisplayString(afterVal))
                        .build();
            }
            changes.add(change);
        }

        return ChangeSet.builder().changes(changes).build();
    }

    /**
     * 递归收集对象的所有字段值（平面化）
     */
    private void collectFieldValues(Object obj, Map<String, Object> values, String prefix) {
        if (obj == null) return;

        Class<?> clazz = obj.getClass();
        for (Field field : getAllFields(clazz)) {
            if (shouldSkip(field)) continue;

            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                String fieldName = resolveFieldName(field, prefix);

                if (value == null) {
                    values.put(fieldName, null);
                } else if (isSimpleType(value.getClass())) {
                    values.put(fieldName, value);
                } else if (value instanceof Collection) {
                    values.put(fieldName, objectMapper.writeValueAsString(value));
                } else if (value instanceof Map) {
                    values.put(fieldName, objectMapper.writeValueAsString(value));
                } else {
                    // 嵌套对象：递归展开
                    String nestedPrefix = prefix.isEmpty() ? field.getName() : prefix + "." + field.getName();
                    collectFieldValues(value, values, nestedPrefix);
                }
            } catch (Exception e) {
                log.debug("Failed to read field '{}': {}", field.getName(), e.getMessage());
            }
        }
    }

    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    private boolean shouldSkip(Field field) {
        int modifiers = field.getModifiers();
        if (java.lang.reflect.Modifier.isStatic(modifiers)) return true;
        if (java.lang.reflect.Modifier.isTransient(modifiers)) return true;
        if (field.isAnnotationPresent(AuditIgnore.class)) return true;
        return false;
    }

    private String resolveFieldName(Field field, String prefix) {
        String baseName;
        if (field.isAnnotationPresent(AuditDiffField.class)) {
            baseName = field.getAnnotation(AuditDiffField.class).value();
        } else {
            baseName = field.getName();
        }
        return prefix.isEmpty() ? baseName : prefix + "." + baseName;
    }

    private boolean isSimpleType(Class<?> type) {
        return type.isPrimitive()
                || type.getName().startsWith("java.lang.")
                || type.getName().startsWith("java.time.")
                || type.getName().startsWith("java.math.")
                || type.isEnum();
    }

    private String toDisplayString(Object value) {
        if (value == null) return null;
        if (value instanceof String) return (String) value;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return value.toString();
        }
    }
}
