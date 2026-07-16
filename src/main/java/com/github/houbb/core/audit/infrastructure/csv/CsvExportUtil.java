package com.github.houbb.core.audit.infrastructure.csv;

import com.github.houbb.core.audit.application.domain.AuditEvent;
import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * CSV 导出工具
 * <p>使用 OpenCSV Streaming 写入，避免大数据量内存溢出。</p>
 */
public final class CsvExportUtil {

    private static final Logger log = LoggerFactory.getLogger(CsvExportUtil.class);
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String[] HEADERS = {
            "ID", "时间", "模块", "操作", "对象类型", "对象ID",
            "操作人ID", "操作人名称", "结果", "描述",
            "客户端IP", "请求URI", "请求方法", "TraceID"
    };

    private CsvExportUtil() {
    }

    /**
     * 将审计事件列表写入 CSV（流式输出）
     *
     * @param events     审计事件列表
     * @param outputStream 输出流
     */
    public static void write(List<AuditEvent> events, OutputStream outputStream) {
        try (CSVWriter writer = new CSVWriter(
                new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {

            // 写入 BOM 确保 Excel 正确识别 UTF-8
            outputStream.write(0xEF);
            outputStream.write(0xBB);
            outputStream.write(0xBF);

            // 写入表头
            writer.writeNext(HEADERS);

            // 写入数据行
            for (AuditEvent event : events) {
                String[] row = new String[HEADERS.length];
                row[0] = nullSafe(event.getId());
                row[1] = event.getCreatedAt() != null ? event.getCreatedAt().format(DT_FMT) : "";
                row[2] = event.getModule() != null ? event.getModule().name() : "";
                row[3] = event.getAction() != null ? event.getAction().name() : "";
                row[4] = nullSafe(event.getTargetType());
                row[5] = nullSafe(event.getTargetId());
                row[6] = nullSafe(event.getOperatorId());
                row[7] = nullSafe(event.getOperatorName());
                row[8] = event.getResult() != null ? event.getResult().name() : "";
                row[9] = nullSafe(event.getDescription());
                row[10] = nullSafe(event.getClientIp());
                row[11] = nullSafe(event.getRequestUri());
                row[12] = nullSafe(event.getRequestMethod());
                row[13] = nullSafe(event.getTraceId());
                writer.writeNext(row);
            }
        } catch (Exception e) {
            log.error("CSV export failed", e);
            throw new RuntimeException("CSV export failed", e);
        }
    }

    private static String nullSafe(String value) {
        return value != null ? value : "";
    }
}