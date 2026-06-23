package com.waterdelivery.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * API响应工具类测试
 */
public class ApiResponseTest {

    @Test
    void testSuccessWithData() {
        ApiResponse<String> response = ApiResponse.success("hello");
        assertEquals(200, response.getCode());
        assertEquals("success", response.getMessage());
        assertEquals("hello", response.getData());
    }

    @Test
    void testSuccessWithoutData() {
        ApiResponse<Void> response = ApiResponse.success();
        assertEquals(200, response.getCode());
        assertEquals("success", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void testFail() {
        ApiResponse<Void> response = ApiResponse.fail("用户名已存在");
        assertEquals(400, response.getCode());
        assertEquals("用户名已存在", response.getMessage());
        assertNull(response.getData());
    }

    @Test
    void testFailEmptyMessage() {
        ApiResponse<Void> response = ApiResponse.fail("");
        assertEquals(400, response.getCode());
        assertEquals("", response.getMessage());
    }
}
