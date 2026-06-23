package com.waterdelivery.config;

import com.waterdelivery.common.CurrentContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT令牌工具测试
 */
public class JwtUtilTest {

    private JwtUtil jwtUtil;
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret("test-secret-key-for-jwt-at-least-32-bytes-long");
        jwtProperties.setExpireHours(24);
        jwtUtil = new JwtUtil(jwtProperties);
        jwtUtil.init();
    }

    @Test
    void testCreateUserToken() {
        String token = jwtUtil.createUserToken(1L, "testuser");
        assertNotNull(token);
        assertFalse(token.isBlank());

        CurrentContext.TokenPayload payload = jwtUtil.parse(token);
        assertEquals("USER", payload.getType());
        assertEquals(1L, payload.getUserId());
        assertEquals("testuser", payload.getUsername());
    }

    @Test
    void testCreateAdminToken() {
        String token = jwtUtil.createAdminToken(1L, "admin", List.of("admin:dashboard", "product:edit"));
        assertNotNull(token);

        CurrentContext.TokenPayload payload = jwtUtil.parse(token);
        assertEquals("ADMIN", payload.getType());
        assertEquals(1L, payload.getAdminId());
        assertEquals("admin", payload.getUsername());
        assertEquals(2, payload.getPermissions().size());
        assertTrue(payload.getPermissions().contains("admin:dashboard"));
        assertTrue(payload.getPermissions().contains("product:edit"));
    }

    @Test
    void testCreateDeliveryToken() {
        String token = jwtUtil.createDeliveryToken(10L, "delivery01");
        assertNotNull(token);

        CurrentContext.TokenPayload payload = jwtUtil.parse(token);
        assertEquals("DELIVERY_STAFF", payload.getType());
        assertEquals(10L, payload.getStaffId());
        assertEquals("delivery01", payload.getUsername());
    }

    @Test
    void testParseTokenWithNoPermissions() {
        String token = jwtUtil.createUserToken(1L, "testuser");

        CurrentContext.TokenPayload payload = jwtUtil.parse(token);
        assertNotNull(payload.getPermissions());
        assertTrue(payload.getPermissions().isEmpty());
    }

    @Test
    void testParseTokenWithNullPermissions() {
        String token = jwtUtil.createUserToken(1L, "testuser");
        CurrentContext.TokenPayload payload = jwtUtil.parse(token);
        assertNull(payload.getStaffId()); // user token 的 staffId 应为 null
        assertNull(payload.getAdminId()); // user token 的 adminId 应为 null
    }

    @Test
    void testParseInvalidToken() {
        assertThrows(Exception.class, () -> jwtUtil.parse("invalid-token"));
    }
}
