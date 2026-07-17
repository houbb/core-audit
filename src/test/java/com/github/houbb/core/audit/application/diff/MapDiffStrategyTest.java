package com.github.houbb.core.audit.application.diff;

import com.github.houbb.core.audit.application.domain.diff.Change;
import com.github.houbb.core.audit.application.domain.diff.ChangeSet;
import com.github.houbb.core.audit.application.domain.enums.ChangeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MapDiffStrategy")
class MapDiffStrategyTest {

    private final MapDiffStrategy strategy = new MapDiffStrategy();

    @Nested
    @DisplayName("Basic map comparison")
    class BasicComparison {

        @Test
        @DisplayName("should detect added key")
        void shouldDetectAddedKey() {
            Map<String, String> before = new HashMap<>();
            before.put("color", "blue");

            Map<String, String> after = new HashMap<>();
            after.put("color", "blue");
            after.put("font", "Arial");

            ChangeSet result = strategy.compare(before, after);

            Change fontChange = findChange(result, "font");
            assertNotNull(fontChange);
            assertEquals(ChangeType.ADD, fontChange.getChangeType());
        }

        @Test
        @DisplayName("should detect removed key")
        void shouldDetectRemovedKey() {
            Map<String, String> before = new HashMap<>();
            before.put("color", "blue");
            before.put("font", "Arial");

            Map<String, String> after = new HashMap<>();
            after.put("color", "blue");

            ChangeSet result = strategy.compare(before, after);

            Change fontChange = findChange(result, "font");
            assertNotNull(fontChange);
            assertEquals(ChangeType.REMOVE, fontChange.getChangeType());
        }

        @Test
        @DisplayName("should detect updated value")
        void shouldDetectUpdatedValue() {
            Map<String, String> before = new HashMap<>();
            before.put("color", "blue");

            Map<String, String> after = new HashMap<>();
            after.put("color", "red");

            ChangeSet result = strategy.compare(before, after);

            Change change = findChange(result, "color");
            assertNotNull(change);
            assertEquals(ChangeType.UPDATE, change.getChangeType());
            assertEquals("blue", change.getBeforeValue());
            assertEquals("red", change.getAfterValue());
        }

        @Test
        @DisplayName("should mark unchanged key")
        void shouldMarkUnchanged() {
            Map<String, String> before = new HashMap<>();
            before.put("color", "blue");

            Map<String, String> after = new HashMap<>();
            after.put("color", "blue");

            ChangeSet result = strategy.compare(before, after);

            Change change = findChange(result, "color");
            assertNotNull(change);
            assertEquals(ChangeType.UNCHANGED, change.getChangeType());
        }
    }

    @Nested
    @DisplayName("supports()")
    class SupportsTests {

        @Test
        @DisplayName("should support HashMap")
        void shouldSupportHashMap() {
            assertTrue(strategy.supports(HashMap.class));
        }

        @Test
        @DisplayName("should support LinkedHashMap")
        void shouldSupportLinkedHashMap() {
            assertTrue(strategy.supports(LinkedHashMap.class));
        }
    }

    private Change findChange(ChangeSet changeSet, String fieldName) {
        return changeSet.getChanges().stream()
                .filter(c -> c.getFieldName().equals(fieldName))
                .findFirst()
                .orElse(null);
    }
}