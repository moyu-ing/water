package com.waterdelivery.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

public class CurrentContext {

    private static final ThreadLocal<TokenPayload> HOLDER = new ThreadLocal<>();

    public static void set(TokenPayload payload) {
        HOLDER.set(payload);
    }

    public static TokenPayload get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        TokenPayload payload = HOLDER.get();
        return payload == null ? null : payload.getUserId();
    }

    public static Long getAdminId() {
        TokenPayload payload = HOLDER.get();
        return payload == null ? null : payload.getAdminId();
    }

    public static Set<String> getPermissions() {
        TokenPayload payload = HOLDER.get();
        return payload == null ? new HashSet<>() : payload.getPermissions();
    }

    public static void clear() {
        HOLDER.remove();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenPayload {
        private String type;
        private Long userId;
        private Long adminId;
        private String username;
        private Set<String> permissions = new HashSet<>();
    }
}
