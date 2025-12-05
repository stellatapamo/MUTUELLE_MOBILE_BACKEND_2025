package com.mutuelle.mobille.utils;

import org.springframework.security.core.context.SecurityContextHolder;
import com.mutuelle.mobille.models.AuthUser;

public class SecurityUtil {
    public static Long getCurrentUserRefId() {
        AuthUser user = (AuthUser) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return user.getUserRefId();
    }

    public static String getCurrentUserEmail() {
        AuthUser user = (AuthUser) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return user.getEmail();
    }
}