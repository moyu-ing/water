package com.waterdelivery.config;

import com.waterdelivery.common.CurrentContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Component
public class JwtUtil {

    private SecretKey key;
    private final JwtProperties jwtProperties;

    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createUserToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "USER");
        claims.put("userId", userId);
        claims.put("username", username);
        return createToken(claims);
    }

    public String createAdminToken(Long adminId, String username, List<String> permissions) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "ADMIN");
        claims.put("adminId", adminId);
        claims.put("username", username);
        claims.put("permissions", permissions);
        return createToken(claims);
    }

    public String createDeliveryToken(Long staffId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "DELIVERY_STAFF");
        claims.put("staffId", staffId);
        claims.put("username", username);
        return createToken(claims);
    }

    private String createToken(Map<String, Object> claims) {
        Instant expireTime = Instant.now().plus(jwtProperties.getExpireHours(), ChronoUnit.HOURS);
        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(Date.from(expireTime))
                .signWith(key)
                .compact();
    }

    @SuppressWarnings("unchecked")
    public CurrentContext.TokenPayload parse(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        CurrentContext.TokenPayload payload = new CurrentContext.TokenPayload();
        payload.setType((String) claims.get("type"));
        payload.setUserId(claims.get("userId", Long.class));
        payload.setAdminId(claims.get("adminId", Long.class));
        payload.setStaffId(claims.get("staffId", Long.class));
        payload.setUsername((String) claims.get("username"));
        Object permissions = claims.get("permissions");
        if (permissions instanceof List<?> permissionList) {
            payload.setPermissions(new HashSet<>(permissionList.stream().map(String::valueOf).toList()));
        }
        return payload;
    }
}
