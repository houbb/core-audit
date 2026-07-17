package com.github.houbb.core.audit.application.diff;

import com.github.houbb.core.audit.application.domain.diff.Change;
import com.github.houbb.core.audit.application.domain.diff.ChangeSet;
import com.github.houbb.core.audit.application.domain.enums.ChangeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BeanDiffStrategy")
class BeanDiffStrategyTest {

    private final BeanDiffStrategy strategy = new BeanDiffStrategy();

    static class SimpleUser {
        String name;
        String role;
        String status;

        SimpleUser() {}
        SimpleUser(String name, String role, String status) {
            this.name = name; this.role = role; this.status = status;
        }
    }

    static class UserWithIgnore {
        String name;
        String role;
        @AuditIgnore
        Integer version;
        @AuditIgnore
        String updateTime;

        UserWithIgnore() {}
        UserWithIgnore(String name, String role, Integer version, String updateTime) {
            this.name = name; this.role = role; this.version = version; this.updateTime = updateTime;
        }
    }

    static class UserWithCustomField {
        @AuditDiffField("用户名称")
        String name;
        @AuditDiffField("角色")
        String role;
    }

    @Nested
    @DisplayName("Basic comparison")
    class BasicComparison {

        @Test
        @DisplayName("should detect UPDATE when field values differ")
        void shouldDetectUpdate() {
            SimpleUser before = new SimpleUser("echo", "USER", "ENABLE");
            SimpleUser after = new SimpleUser("echo-admin", "ADMIN", "DISABLED");

            ChangeSet result = strategy.compare(before, after);

            assertEquals(3, result.getChanges().size());
            assertTrue(result.hasChanges());
            assertEquals(3, result.changedCount());

            Change nameChange = findChange(result, "name");
            assertEquals(ChangeType.UPDATE, nameChange.getChangeType());
            assertEquals("echo", nameChange.getBeforeValue());
            assertEquals("echo-admin", nameChange.getAfterValue());
        }

        @Test
        @DisplayName("should detect ADD when field goes from null to value")
        void shouldDetectAdd() {
            SimpleUser before = new SimpleUser("echo", "USER", null);
            SimpleUser after = new SimpleUser("echo", "USER", "ENABLE");

            ChangeSet result = strategy.compare(before, after);
            Change change = findChange(result, "status");
            assertEquals(ChangeType.ADD, change.getChangeType());
            assertNull(change.getBeforeValue());
            assertNotNull(change.getAfterValue());
        }

        @Test
        @DisplayName("should detect REMOVE when field goes from value to null")
        void shouldDetectRemove() {
            SimpleUser before = new SimpleUser("echo", "USER", "ENABLE");
            SimpleUser after = new SimpleUser("echo", "USER", null);

            ChangeSet result = strategy.compare(before, after);
            Change change = findChange(result, "status");
            assertEquals(ChangeType.REMOVE, change.getChangeType());
            assertNotNull(change.getBeforeValue());
            assertNull(change.getAfterValue());
        }

        @Test
        @DisplayName("should mark UNCHANGED when fields are equal")
        void shouldMarkUnchanged() {
            SimpleUser before = new SimpleUser("echo", "USER", "ENABLE");
            SimpleUser after = new SimpleUser("echo", "USER", "ENABLE");

            ChangeSet result = strategy.compare(before, after);
            assertFalse(result.hasChanges());
            assertEquals(0, result.changedCount());
        }
    }

    @Nested
    @DisplayName("@AuditIgnore")
    class AuditIgnoreTests {

        @Test
        @DisplayName("should skip fields annotated with @AuditIgnore")
        void shouldSkipIgnoredFields() {
            UserWithIgnore before = new UserWithIgnore("echo", "USER", 1, "2024-01-01");
            UserWithIgnore after = new UserWithIgnore("echo-admin", "ADMIN", 2, "2024-01-02");

            ChangeSet result = strategy.compare(before, after);

            // version and updateTime should be skipped
            assertNull(findChange(result, "version"));
            assertNull(findChange(result, "updateTime"));

            // name and role should still be compared
            assertNotNull(findChange(result, "name"));
            assertNotNull(findChange(result, "role"));
        }
    }

    @Nested
    @DisplayName("@AuditDiffField")
    class AuditDiffFieldTests {

        @Test
        @DisplayName("should use custom field name from @AuditDiffField")
        void shouldUseCustomFieldName() {
            UserWithCustomField before = new UserWithCustomField();
            before.name = "echo";
            before.role = "USER";
            UserWithCustomField after = new UserWithCustomField();
            after.name = "echo-admin";
            after.role = "ADMIN";

            ChangeSet result = strategy.compare(before, after);

            assertNotNull(findChange(result, "用户名称"));
            assertNotNull(findChange(result, "角色"));
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @Test
        @DisplayName("should handle null before object")
        void shouldHandleNullBefore() {
            SimpleUser after = new SimpleUser("echo", "USER", "ENABLE");
            ChangeSet result = strategy.compare(null, after);
            assertNotNull(result);
        }

        @Test
        @DisplayName("should handle null after object")
        void shouldHandleNullAfter() {
            SimpleUser before = new SimpleUser("echo", "USER", "ENABLE");
            ChangeSet result = strategy.compare(before, null);
            assertNotNull(result);
        }

        @Test
        @DisplayName("should handle both null")
        void shouldHandleBothNull() {
            ChangeSet result = strategy.compare(null, null);
            assertNotNull(result);
            assertEquals(0, result.getChanges().size());
        }
    }

    @Nested
    @DisplayName("supports()")
    class SupportsTests {

        @Test
        @DisplayName("should support POJO classes")
        void shouldSupportPojo() {
            assertTrue(strategy.supports(SimpleUser.class));
        }

        @Test
        @DisplayName("should not support String")
        void shouldNotSupportString() {
            assertFalse(strategy.supports(String.class));
        }

        @Test
        @DisplayName("should not support List")
        void shouldNotSupportList() {
            assertFalse(strategy.supports(ArrayList.class));
        }

        @Test
        @DisplayName("should not support Map")
        void shouldNotSupportMap() {
            assertFalse(strategy.supports(HashMap.class));
        }
    }

    private Change findChange(ChangeSet changeSet, String fieldName) {
        return changeSet.getChanges().stream()
                .filter(c -> c.getFieldName().equals(fieldName))
                .findFirst()
                .orElse(null);
    }
}