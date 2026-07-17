package com.github.houbb.core.audit.application.diff;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.houbb.core.audit.application.domain.diff.Change;
import com.github.houbb.core.audit.application.domain.diff.ChangeSet;
import com.github.houbb.core.audit.application.domain.enums.ChangeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * JSON 节点比较策略
 * <p>使用 Jackson JsonNode 对 JSON 对象进行逐字段递归比较。</p>
 * <p>适用于非 Java 对象的通用 JSON 负载比较。</p>
 */
@Component
public class JsonDiffStrategy implements DiffStrategy<JsonNode> {

    private static final Logger log = LoggerFactory.getLogger(JsonDiffStrategy.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(Class<?> type) {
        return JsonNode.class.isAssignableFrom(type);
    }

    @Override
    public ChangeSet compare(JsonNode before, JsonNode after) {
        List<Change> changes = new ArrayList<>();
        compareNodes(before, after, "", changes);
        return ChangeSet.builder().changes(changes).build();
    }

    private void compareNodes(JsonNode before, JsonNode after, String path, List<Change> changes) {
        if (before == null && after == null) return;

        if (before == null && after != null) {
            if (after.isObject()) {
                after.fields().forEachRemaining(entry ->
                        compareNodes(null, entry.getValue(), joinPath(path, entry.getKey()), changes));
            } else {
                changes.add(Change.builder()
                        .fieldName(path.isEmpty() ? "root" : path)
                        .changeType(ChangeType.ADD)
                        .beforeValue(null)
                        .afterValue(nodeToString(after))
                        .build());
            }
            return;
        }

        if (after == null && before != null) {
            if (before.isObject()) {
                before.fields().forEachRemaining(entry ->
                        compareNodes(entry.getValue(), null, joinPath(path, entry.getKey()), changes));
            } else {
                changes.add(Change.builder()
                        .fieldName(path.isEmpty() ? "root" : path)
                        .changeType(ChangeType.REMOVE)
                        .beforeValue(nodeToString(before))
                        .afterValue(null)
                        .build());
            }
            return;
        }

        if (before.isArray() && after.isArray()) {
            compareArrays(before, after, path, changes);
            return;
        }

        if (before.isObject() && after.isObject()) {
            Set<String> allFields = new LinkedHashSet<>();
            before.fieldNames().forEachRemaining(allFields::add);
            after.fieldNames().forEachRemaining(allFields::add);

            for (String field : allFields) {
                JsonNode beforeChild = before.get(field);
                JsonNode afterChild = after.get(field);
                compareNodes(beforeChild, afterChild, joinPath(path, field), changes);
            }
            return;
        }

        // 叶子节点比较
        if (!before.equals(after)) {
            changes.add(Change.builder()
                    .fieldName(path.isEmpty() ? "root" : path)
                    .changeType(ChangeType.UPDATE)
                    .beforeValue(nodeToString(before))
                    .afterValue(nodeToString(after))
                    .build());
        } else {
            changes.add(Change.builder()
                    .fieldName(path.isEmpty() ? "root" : path)
                    .changeType(ChangeType.UNCHANGED)
                    .beforeValue(nodeToString(before))
                    .afterValue(nodeToString(after))
                    .build());
        }
    }

    private void compareArrays(JsonNode before, JsonNode after, String path, List<Change> changes) {
        int maxSize = Math.max(before.size(), after.size());
        for (int i = 0; i < maxSize; i++) {
            JsonNode beforeElem = i < before.size() ? before.get(i) : null;
            JsonNode afterElem = i < after.size() ? after.get(i) : null;
            String elemPath = path + "[" + i + "]";

            if (beforeElem == null && afterElem != null) {
                changes.add(Change.builder()
                        .fieldName(elemPath)
                        .changeType(ChangeType.ADD)
                        .beforeValue(null)
                        .afterValue(nodeToString(afterElem))
                        .build());
            } else if (beforeElem != null && afterElem == null) {
                changes.add(Change.builder()
                        .fieldName(elemPath)
                        .changeType(ChangeType.REMOVE)
                        .beforeValue(nodeToString(beforeElem))
                        .afterValue(null)
                        .build());
            } else if (!beforeElem.equals(afterElem)) {
                compareNodes(beforeElem, afterElem, elemPath, changes);
            }
        }
    }

    private String joinPath(String prefix, String key) {
        return prefix.isEmpty() ? key : prefix + "." + key;
    }

    private String nodeToString(JsonNode node) {
        if (node == null) return null;
        if (node.isTextual()) return node.asText();
        try {
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            return node.toString();
        }
    }
}
