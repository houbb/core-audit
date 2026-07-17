package com.github.houbb.core.audit.application.diff;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.houbb.core.audit.application.domain.diff.Change;
import com.github.houbb.core.audit.application.domain.diff.ChangeSet;
import com.github.houbb.core.audit.application.domain.enums.ChangeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JsonDiffStrategy")
class JsonDiffStrategyTest {

    private final JsonDiffStrategy strategy = new JsonDiffStrategy();
    private static final ObjectMapper mapper = new ObjectMapper();

    @Nested
    @DisplayName("Basic JSON comparison")
    class BasicComparison {

        @Test
        @DisplayName("should detect field update in flat JSON")
        void shouldDetectUpdate() {
            ObjectNode before = mapper.createObjectNode()
                    .put("name", "echo")
                    .put("role", "USER");
            ObjectNode after = mapper.createObjectNode()
                    .put("name", "echo-admin")
                    .put("role", "ADMIN");

            ChangeSet result = strategy.compare(before, after);

            assertTrue(result.hasChanges());
            assertEquals(2, result.changedCount());
        }

        @Test
        @DisplayName("should detect added field")
        void shouldDetectAddedField() {
            ObjectNode before = mapper.createObjectNode().put("name", "echo");
            ObjectNode after = mapper.createObjectNode()
                    .put("name", "echo")
                    .put("phone", "138xxxx");

            ChangeSet result = strategy.compare(before, after);

            Change phoneChange = findChange(result, "phone");
            assertNotNull(phoneChange);
            assertEquals(ChangeType.ADD, phoneChange.getChangeType());
        }

        @Test
        @DisplayName("should detect removed field")
        void shouldDetectRemovedField() {
            ObjectNode before = mapper.createObjectNode()
                    .put("name", "echo")
                    .put("remark", "hello");
            ObjectNode after = mapper.createObjectNode().put("name", "echo");

            ChangeSet result = strategy.compare(before, after);

            Change remarkChange = findChange(result, "remark");
            assertNotNull(remarkChange);
            assertEquals(ChangeType.REMOVE, remarkChange.getChangeType());
        }
    }

    @Nested
    @DisplayName("Nested object comparison")
    class NestedComparison {

        @Test
        @DisplayName("should handle nested objects with dot notation")
        void shouldHandleNestedObjects() {
            ObjectNode addressBefore = mapper.createObjectNode()
                    .put("city", "Beijing")
                    .put("street", "Chang'an");
            ObjectNode addressAfter = mapper.createObjectNode()
                    .put("city", "Shanghai")
                    .put("street", "Nanjing");

            ObjectNode before = mapper.createObjectNode().set("address", addressBefore);
            ObjectNode after = mapper.createObjectNode().set("address", addressAfter);

            ChangeSet result = strategy.compare(before, after);

            assertTrue(result.hasChanges());
            // Should have changes for city and street
            assertNotNull(findChange(result, "address.city"));
            assertNotNull(findChange(result, "address.street"));
        }
    }

    @Nested
    @DisplayName("Array comparison")
    class ArrayComparison {

        @Test
        @DisplayName("should detect array element addition")
        void shouldDetectArrayAddition() throws Exception {
            String beforeJson = "{\"roles\":[\"ADMIN\",\"OPS\"]}";
            String afterJson = "{\"roles\":[\"ADMIN\",\"OPS\",\"DEV\"]}";
            com.fasterxml.jackson.databind.JsonNode beforeNode = mapper.readTree(beforeJson);
            com.fasterxml.jackson.databind.JsonNode afterNode = mapper.readTree(afterJson);

            ChangeSet result = strategy.compare(beforeNode, afterNode);

            // Should detect the added DEV element
            Change addChange = findChange(result, "roles[2]");
            assertNotNull(addChange);
            assertEquals(ChangeType.ADD, addChange.getChangeType());
        }
    }

    @Nested
    @DisplayName("supports()")
    class SupportsTests {

        @Test
        @DisplayName("should support JsonNode")
        void shouldSupportJsonNode() {
            assertTrue(strategy.supports(com.fasterxml.jackson.databind.JsonNode.class));
        }

        @Test
        @DisplayName("should not support plain Object")
        void shouldNotSupportObject() {
            assertFalse(strategy.supports(Object.class));
        }
    }

    private Change findChange(ChangeSet changeSet, String fieldName) {
        return changeSet.getChanges().stream()
                .filter(c -> c.getFieldName().equals(fieldName))
                .findFirst()
                .orElse(null);
    }
}