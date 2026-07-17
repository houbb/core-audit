package com.github.houbb.core.audit.application.diff;

import com.github.houbb.core.audit.application.domain.diff.Change;
import com.github.houbb.core.audit.application.domain.diff.ChangeSet;
import com.github.houbb.core.audit.application.domain.enums.ChangeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CollectionDiffStrategy")
class CollectionDiffStrategyTest {

    private final CollectionDiffStrategy strategy = new CollectionDiffStrategy();

    @Nested
    @DisplayName("List comparison")
    class ListComparison {

        @Test
        @DisplayName("should detect added element")
        void shouldDetectAddedElement() {
            List<String> before = new ArrayList<>(List.of("A", "B"));
            List<String> after = new ArrayList<>(List.of("A", "B", "C"));

            ChangeSet result = strategy.compare(before, after);

            Change addChange = findChange(result, "items[2]");
            assertNotNull(addChange);
            assertEquals(ChangeType.ADD, addChange.getChangeType());
        }

        @Test
        @DisplayName("should detect removed element")
        void shouldDetectRemovedElement() {
            List<String> before = new ArrayList<>(List.of("A", "B", "C"));
            List<String> after = new ArrayList<>(List.of("A", "B"));

            ChangeSet result = strategy.compare(before, after);

            Change removeChange = findChange(result, "items[2]");
            assertNotNull(removeChange);
            assertEquals(ChangeType.REMOVE, removeChange.getChangeType());
        }

        @Test
        @DisplayName("should detect updated element")
        void shouldDetectUpdatedElement() {
            List<String> before = new ArrayList<>(List.of("A", "B"));
            List<String> after = new ArrayList<>(List.of("A", "C"));

            ChangeSet result = strategy.compare(before, after);

            Change updateChange = findChange(result, "items[1]");
            assertNotNull(updateChange);
            assertEquals(ChangeType.UPDATE, updateChange.getChangeType());
        }
    }

    @Nested
    @DisplayName("supports()")
    class SupportsTests {

        @Test
        @DisplayName("should support ArrayList")
        void shouldSupportArrayList() {
            assertTrue(strategy.supports(ArrayList.class));
        }

        @Test
        @DisplayName("should support HashSet")
        void shouldSupportHashSet() {
            assertTrue(strategy.supports(HashSet.class));
        }
    }

    private Change findChange(ChangeSet changeSet, String fieldName) {
        return changeSet.getChanges().stream()
                .filter(c -> c.getFieldName().equals(fieldName))
                .findFirst()
                .orElse(null);
    }
}