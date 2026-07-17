package com.github.houbb.core.audit.application.diff;

import com.github.houbb.core.audit.application.domain.diff.Change;
import com.github.houbb.core.audit.application.domain.diff.ChangeSet;
import com.github.houbb.core.audit.application.domain.enums.ChangeType;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Map 比较策略
 * <p>比较 Map 的 key 级增减改。</p>
 * <p>字段名格式为 "config.keyName"。</p>
 */
@Component
public class MapDiffStrategy implements DiffStrategy<Map<?, ?>> {

    @Override
    public boolean supports(Class<?> type) {
        return Map.class.isAssignableFrom(type);
    }

    @Override
    public ChangeSet compare(Map<?, ?> before, Map<?, ?> after) {
        List<Change> changes = new ArrayList<>();

        if (before == null && after == null) {
            return ChangeSet.builder().changes(changes).build();
        }

        Set<Object> allKeys = new LinkedHashSet<>();
        if (before != null) allKeys.addAll(before.keySet());
        if (after != null) allKeys.addAll(after.keySet());

        for (Object key : allKeys) {
            Object beforeVal = before != null ? before.get(key) : null;
            Object afterVal = after != null ? after.get(key) : null;
            String fieldName = key.toString();

            Change change;
            if (beforeVal == null && afterVal == null) {
                continue;
            } else if (beforeVal == null && afterVal != null) {
                change = Change.builder()
                        .fieldName(fieldName)
                        .changeType(ChangeType.ADD)
                        .beforeValue(null)
                        .afterValue(afterVal.toString())
                        .build();
            } else if (beforeVal != null && afterVal == null) {
                change = Change.builder()
                        .fieldName(fieldName)
                        .changeType(ChangeType.REMOVE)
                        .beforeValue(beforeVal.toString())
                        .afterValue(null)
                        .build();
            } else if (Objects.equals(beforeVal, afterVal)) {
                change = Change.builder()
                        .fieldName(fieldName)
                        .changeType(ChangeType.UNCHANGED)
                        .beforeValue(beforeVal.toString())
                        .afterValue(afterVal.toString())
                        .build();
            } else {
                change = Change.builder()
                        .fieldName(fieldName)
                        .changeType(ChangeType.UPDATE)
                        .beforeValue(beforeVal.toString())
                        .afterValue(afterVal.toString())
                        .build();
            }
            changes.add(change);
        }

        return ChangeSet.builder().changes(changes).build();
    }
}
