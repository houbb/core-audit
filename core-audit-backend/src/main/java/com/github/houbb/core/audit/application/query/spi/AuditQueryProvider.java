package com.github.houbb.core.audit.application.query.spi;

import com.github.houbb.core.audit.application.domain.query.AuditQuery;

import java.util.List;

/**
 * 审计查询条件提供者（SPI 扩展点）
 * <p>每个 Provider 负责一类查询维度的过滤条件。新模块可以通过实现此接口
 * 来增加自定义查询维度，无需修改 Query Engine 核心代码。</p>
 *
 * <p>内置 5 个默认 Provider：</p>
 * <ul>
 *   <li>BasicQueryProvider — 模块/操作/结果/事件类型/目标对象</li>
 *   <li>ContextQueryProvider — 操作人/租户/部门/IP/浏览器等上下文</li>
 *   <li>DiffQueryProvider — 变更字段/变更前后值/变更类型</li>
 *   <li>KeywordQueryProvider — 全文关键字搜索</li>
 *   <li>MetadataQueryProvider — JSON Metadata 键值过滤</li>
 * </ul>
 */
public interface AuditQueryProvider {

    /**
     * 返回此 Provider 的名称（用于调试和文档）
     */
    String getName();

    /**
     * 优先级，值越小越先执行
     * <p>建议范围：100=Basic, 200=Context, 300=Diff, 400=Keyword, 500=Metadata</p>
     */
    int order();

    /**
     * 判断当前查询是否需要此 Provider 参与
     *
     * @param query 查询条件
     * @return true 表示需要此 Provider 追加过滤条件
     */
    boolean supports(AuditQuery query);

    /**
     * 向 SQL builder 追加此 Provider 的过滤条件
     *
     * @param sql    当前 SQL builder（已含 "WHERE 1=1"）
     * @param params 参数列表
     * @param query  原始查询条件
     */
    void contribute(StringBuilder sql, List<Object> params, AuditQuery query);
}