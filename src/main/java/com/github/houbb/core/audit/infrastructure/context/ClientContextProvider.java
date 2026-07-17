package com.github.houbb.core.audit.infrastructure.context;

import com.github.houbb.core.audit.application.context.AuditContextProvider;
import com.github.houbb.core.audit.application.domain.context.AuditContext;
import com.github.houbb.core.audit.application.domain.context.ClientContext;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 客户端上下文提供者
 * <p>从 HTTP 请求头解析 IP、浏览器、操作系统、设备、语言、时区等信息。</p>
 * <p>使用简单正则解析 User-Agent，不引入外部 UA 解析库。</p>
 * <p>P2 不做 Geo IP（留到 P8），region 字段暂时为空。</p>
 */
@Component
public class ClientContextProvider implements AuditContextProvider {

    private static final Logger log = LoggerFactory.getLogger(ClientContextProvider.class);

    @Override
    public int order() {
        return 300;
    }

    @Override
    public void contribute(AuditContext context) {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes)
                    RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attrs.getRequest();

            ClientContext clientCtx = new ClientContext();

            // IP: 优先 X-Forwarded-For
            String ip = request.getHeader("X-Forwarded-For");
            if (isEmpty(ip)) {
                ip = request.getHeader("X-Real-IP");
            }
            if (isEmpty(ip)) {
                ip = request.getRemoteAddr();
            }
            clientCtx.setIp(ip);

            // User-Agent 解析
            String ua = request.getHeader("User-Agent");
            if (!isEmpty(ua)) {
                clientCtx.setBrowser(parseBrowser(ua));
                clientCtx.setOs(parseOs(ua));
                clientCtx.setDevice(parseDevice(ua));
            }

            // Language
            String lang = request.getHeader("Accept-Language");
            if (!isEmpty(lang)) {
                // 取第一个语言标签
                String primaryLang = lang.split(",")[0].split(";")[0].trim();
                clientCtx.setLanguage(primaryLang);
            }

            // Timezone
            String tz = request.getHeader("X-Timezone");
            clientCtx.setTimezone(tz);

            context.setClient(clientCtx);
        } catch (IllegalStateException e) {
            log.debug("No request context available — skipping ClientContextProvider");
        }
    }

    // ======== Simple UA Parsing ========

    private String parseBrowser(String ua) {
        if (ua.contains("Edg/")) return "Edge";
        if (ua.contains("Chrome/")) return "Chrome";
        if (ua.contains("Safari/") && !ua.contains("Chrome")) return "Safari";
        if (ua.contains("Firefox/")) return "Firefox";
        if (ua.contains("OPR/") || ua.contains("Opera/")) return "Opera";
        return null;
    }

    private String parseOs(String ua) {
        if (ua.contains("Windows")) return "Windows";
        if (ua.contains("Mac OS")) return "macOS";
        if (ua.contains("Linux") && !ua.contains("Android")) return "Linux";
        if (ua.contains("Android")) return "Android";
        if (ua.contains("iPhone") || ua.contains("iPad")) return "iOS";
        return null;
    }

    private String parseDevice(String ua) {
        if (ua.contains("Mobile")) return "Mobile";
        if (ua.contains("Tablet") || ua.contains("iPad")) return "Tablet";
        if (ua.contains("Windows") || ua.contains("Macintosh") || ua.contains("Linux")) return "Desktop";
        return null;
    }

    private boolean isEmpty(String s) {
        return s == null || s.isBlank();
    }
}
