package com.github.houbb.core.audit.application.domain.enums;

/**
 * 时间线类型枚举
 * <p>P5 新增：定义 Timeline 的聚合维度。</p>
 *
 * <p>USER — 按操作人聚合，表达"谁做了什么"</p>
 * <p>OBJECT — 按目标对象聚合，表达"对象经历了什么"</p>
 * <p>REQUEST — 按 Trace ID 聚合，表达"一个请求经历了哪些服务"</p>
 * <p>SESSION — 按会话聚合，表达"一次会话内发生了什么"</p>
 * <p>INCIDENT — 按事故聚合，表达"事故是如何演进的"</p>
 * <p>WORKFLOW — 按工作流聚合，表达"工作流如何流转"</p>
 */
public enum TimelineType {

    USER,

    OBJECT,

    REQUEST,

    SESSION,

    INCIDENT,

    WORKFLOW

}
