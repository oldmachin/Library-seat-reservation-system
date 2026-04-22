package com.anonymous.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new RuntimeException("当前会话未授信或已过期");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof String) {
            return Long.valueOf((String) principal);
        }

        if (principal instanceof Number) {
            return ((Number) principal).longValue();
        }

        throw new RuntimeException("无法识别当前用户身份");
    }
}
