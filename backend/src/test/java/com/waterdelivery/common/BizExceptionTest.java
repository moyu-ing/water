package com.waterdelivery.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 业务异常类测试
 */
public class BizExceptionTest {

    @Test
    void testCreateException() {
        BizException ex = new BizException("用户名已存在");
        assertEquals("用户名已存在", ex.getMessage());
        assertTrue(ex instanceof RuntimeException);
    }

    @Test
    void testExceptionThrown() {
        try {
            throw new BizException("测试异常");
        } catch (BizException ex) {
            assertEquals("测试异常", ex.getMessage());
        }
    }
}
