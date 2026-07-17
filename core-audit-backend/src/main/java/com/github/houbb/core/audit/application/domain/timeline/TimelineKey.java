package com.github.houbb.core.audit.application.domain.timeline;

import java.util.Objects;

/**
 * 时间线标识符 — 值对象
 * <p>由 type + key 唯一确定一条 Timeline，用作 find-or-create 的依据。</p>
 * <p>不可变，equals/hashCode 基于 type 和 key。</p>
 */
public class TimelineKey {

    private final String type;
    private final String key;

    private TimelineKey(String type, String key) {
        this.type = type;
        this.key = key;
    }

    /** 静态工厂方法 */
    public static TimelineKey of(String type, String key) {
        return new TimelineKey(type, key);
    }

    public String getType() { return type; }
    public String getKey() { return key; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimelineKey)) return false;
        TimelineKey that = (TimelineKey) o;
        return Objects.equals(type, that.type) && Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, key);
    }

    @Override
    public String toString() {
        return "TimelineKey{type='" + type + "', key='" + key + "'}";
    }
}
