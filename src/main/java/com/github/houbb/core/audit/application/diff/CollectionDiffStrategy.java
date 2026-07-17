package com.github.houbb.core.audit.application.diff;

import com.github.houbb.core.audit.application.domain.diff.Change;
import com.github.houbb.core.audit.application.domain.diff.ChangeSet;
import com.github.houbb.core.audit.application.domain.enums.ChangeType;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 集合比较策略
 * <p>比较 List / Set 的元素增删。</p>
 * <p>元素使用 equals 比较，属于同一个集合类型的实例。</p>
 */
@Component
public class CollectionDiffStrategy implements DiffStrategy<Collection<?>> {

    @Override
    public boolean supports(Class<?> type) {
        return Collection.class.isAssignableFrom(type);
    }

    @Override
    public ChangeSet compare(Collection<?> before, Collection<?> after) {
        List<Change> changes = new ArrayList<>();

        if (before == null && after == null) {
            return ChangeSet.builder().changes(changes).build();
        }

        List<?> beforeList = before != null ? new ArrayList<>(before) : Collections.emptyList();
        List<?> afterList = after != null ? new ArrayList<>(after) : Collections.emptyList();

        int maxSize = Math.max(beforeList.size(), afterList.size());

        for (int i = 0; i < maxSize; i++) {
            Object beforeElem = i < beforeList.size() ? beforeList.get(i) : null;
            Object afterElem = i < afterList.size() ? afterList.get(i) : null;
            String fieldName = "items[" + i + "]";

            Change change;
            if (beforeElem == null && afterElem == null) {
                continue;
            } else if (beforeElem == null && afterElem != null) {
                change = Change.builder()
                        .fieldName(fieldName)
                        .changeType(ChangeType.ADD)
                        .beforeValue(null)
                        .afterValue(afterElem.toString())
                        .build();
            } else if (beforeElem != null && afterElem == null) {
                change = Change.builder()
                        .fieldName(fieldName)
                        .changeType(ChangeType.REMOVE)
                        .beforeValue(beforeElem.toString())
                        .afterValue(null)
                        .build();
            } else if (Objects.equals(beforeElem, afterElem)) {
                change = Change.builder()
                        .fieldName(fieldName)
                        .changeType(ChangeType.UNCHANGED)
                        .beforeValue(beforeElem.toString())
                        .afterValue(afterElem.toString())
                        .build();
            } else {
                change = Change.builder()
                        .fieldName(fieldName)
                        .changeType(ChangeType.UPDATE)
                        .beforeValue(beforeElem.toString())
                        .afterValue(afterElem.toString())
                        .build();
            }
            changes.add(change);
        }

        return ChangeSet.builder().changes(changes).build();
    }
}
