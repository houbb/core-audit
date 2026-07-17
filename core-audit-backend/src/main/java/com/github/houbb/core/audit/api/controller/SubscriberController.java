package com.github.houbb.core.audit.api.controller;

import com.github.houbb.core.audit.application.event.AuditEventSubscriber;
import com.github.houbb.core.audit.application.event.EventBus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Subscriber API
 * <p>查看所有已注册的 Event Subscriber 及其订阅的事件类型。</p>
 */
@RestController
@RequestMapping("/api/v1/audit")
@Tag(name = "Subscribers", description = "Event Subscriber Management API")
public class SubscriberController {

    private final EventBus eventBus;

    public SubscriberController(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @GetMapping("/subscribers")
    @Operation(summary = "列出所有订阅者", description = "返回所有注册的 Event Subscriber 及其订阅的事件类型")
    public ResponseEntity<List<SubscriberInfo>> listSubscribers() {
        List<SubscriberInfo> subscribers = eventBus.getSubscribers().stream()
                .map(SubscriberInfo::from)
                .toList();
        return ResponseEntity.ok(subscribers);
    }

    /**
     * Subscriber 信息 DTO
     */
    public static class SubscriberInfo {
        private String name;
        private List<String> listening;
        private String description;

        public static SubscriberInfo from(AuditEventSubscriber subscriber) {
            SubscriberInfo info = new SubscriberInfo();
            info.name = subscriber.getName();
            info.listening = subscriber.subscribedTypes().isEmpty()
                    ? List.of("ALL")
                    : subscriber.subscribedTypes().stream().map(Enum::name).toList();
            info.description = subscriber.subscribedTypes().isEmpty()
                    ? "监听全部事件"
                    : "监听 " + subscriber.subscribedTypes().size() + " 种事件类型";
            return info;
        }

        public String getName() { return name; }
        public List<String> getListening() { return listening; }
        public String getDescription() { return description; }
    }
}