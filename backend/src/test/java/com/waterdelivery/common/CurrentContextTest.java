package com.waterdelivery.common;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 线程上下文工具类测试
 */
public class CurrentContextTest {

    @BeforeEach
    void setUp() {
        CurrentContext.clear();
    }

    @AfterEach
    void tearDown() {
        CurrentContext.clear();
    }

    @Test
    void testSetAndGet() {
        CurrentContext.TokenPayload payload = new CurrentContext.TokenPayload();
        payload.setType("USER");
        payload.setUserId(1L);
        payload.setUsername("testuser");

        CurrentContext.set(payload);

        CurrentContext.TokenPayload retrieved = CurrentContext.get();
        assertEquals("USER", retrieved.getType());
        assertEquals(1L, retrieved.getUserId());
        assertEquals("testuser", retrieved.getUsername());
    }

    @Test
    void testGetUserId() {
        CurrentContext.TokenPayload payload = new CurrentContext.TokenPayload();
        payload.setUserId(1L);
        CurrentContext.set(payload);

        assertEquals(1L, CurrentContext.getUserId());
    }

    @Test
    void testGetAdminId() {
        CurrentContext.TokenPayload payload = new CurrentContext.TokenPayload();
        payload.setAdminId(100L);
        CurrentContext.set(payload);

        assertEquals(100L, CurrentContext.getAdminId());
    }

    @Test
    void testGetStaffId() {
        CurrentContext.TokenPayload payload = new CurrentContext.TokenPayload();
        payload.setStaffId(10L);
        CurrentContext.set(payload);

        assertEquals(10L, CurrentContext.getStaffId());
    }

    @Test
    void testGetUserIdWhenEmpty() {
        assertNull(CurrentContext.getUserId());
    }

    @Test
    void testGetAdminIdWhenEmpty() {
        assertNull(CurrentContext.getAdminId());
    }

    @Test
    void testGetStaffIdWhenEmpty() {
        assertNull(CurrentContext.getStaffId());
    }

    @Test
    void testGetPermissionsWhenEmpty() {
        Set<String> permissions = CurrentContext.getPermissions();
        assertNotNull(permissions);
        assertTrue(permissions.isEmpty());
    }

    @Test
    void testGetPermissions() {
        CurrentContext.TokenPayload payload = new CurrentContext.TokenPayload();
        payload.setPermissions(Set.of("admin:dashboard", "product:edit"));
        CurrentContext.set(payload);

        Set<String> permissions = CurrentContext.getPermissions();
        assertEquals(2, permissions.size());
        assertTrue(permissions.contains("admin:dashboard"));
    }

    @Test
    void testClear() {
        CurrentContext.TokenPayload payload = new CurrentContext.TokenPayload();
        payload.setUserId(1L);
        CurrentContext.set(payload);

        CurrentContext.clear();
        assertNull(CurrentContext.get());
        assertNull(CurrentContext.getUserId());
    }
}
