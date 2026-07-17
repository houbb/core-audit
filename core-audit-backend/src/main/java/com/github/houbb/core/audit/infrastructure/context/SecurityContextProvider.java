package com.github.houbb.core.audit.infrastructure.context;

import com.github.houbb.core.audit.application.context.AuditContextProvider;
import com.github.houbb.core.audit.application.domain.context.AuditContext;
import com.github.houbb.core.audit.application.domain.context.OperatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 安全上下文提供者
 * <p>尝试从 Spring Security 的 SecurityContextHolder 自动提取当前登录用户信息。</p>
 * <p>使用反射避免硬依赖 Spring Security — 如果不在 classpath 或用户未认证，优雅跳过。</p>
 */
@Component
public class SecurityContextProvider implements AuditContextProvider {

    private static final Logger log = LoggerFactory.getLogger(SecurityContextProvider.class);

    @Override
    public int order() {
        return 200;
    }

    @Override
    public void contribute(AuditContext context) {
        try {
            // 使用反射访问 Spring Security，避免硬依赖
            Class<?> securityContextHolderClass = Class.forName(
                    "org.springframework.security.core.context.SecurityContextHolder");
            Object securityContext = securityContextHolderClass
                    .getMethod("getContext").invoke(null);
            Object authentication = securityContext.getClass()
                    .getMethod("getAuthentication").invoke(securityContext);

            if (authentication == null) {
                log.debug("No authentication — skipping SecurityContextProvider");
                return;
            }

            Boolean isAuthenticated = (Boolean) authentication.getClass()
                    .getMethod("isAuthenticated").invoke(authentication);
            if (isAuthenticated == null || !isAuthenticated) {
                log.debug("Not authenticated — skipping SecurityContextProvider");
                return;
            }

            OperatorContext opCtx = new OperatorContext();

            // username
            Object name = authentication.getClass().getMethod("getName").invoke(authentication);
            if (name != null) {
                opCtx.setUsername(name.toString());
            }

            // authorities → role (first one found)
            Object authorities = authentication.getClass().getMethod("getAuthorities").invoke(authentication);
            if (authorities instanceof java.util.Collection<?> authList && !authList.isEmpty()) {
                Object firstAuth = authList.iterator().next();
                if (firstAuth != null) {
                    String authority = firstAuth.getClass().getMethod("getAuthority").invoke(firstAuth).toString();
                    opCtx.setRole(authority);
                }
            }

            // Try to extract from principal if it's a UserDetails
            Object principal = authentication.getClass().getMethod("getPrincipal").invoke(authentication);
            if (principal != null) {
                try {
                    Class<?> userDetailsClass = Class.forName(
                            "org.springframework.security.core.userdetails.UserDetails");
                    if (userDetailsClass.isAssignableFrom(principal.getClass())) {
                        String uname = (String) userDetailsClass.getMethod("getUsername").invoke(principal);
                        opCtx.setUsername(uname);
                    }
                } catch (ClassNotFoundException e) {
                    // UserDetails not on classpath — fine
                }
            }

            context.setOperator(opCtx);
        } catch (ClassNotFoundException e) {
            log.debug("Spring Security not on classpath — skipping SecurityContextProvider");
        } catch (Exception e) {
            log.debug("Security context unavailable — skipping SecurityContextProvider: {}", e.getMessage());
        }
    }
}
