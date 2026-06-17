package com.waterdelivery.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordUtilTest {

    @Test
    void testPasswordEncodeAndMatch() {
        PasswordUtil util = new PasswordUtil();

        String raw = "123456";

        String encoded = util.encode(raw);

        assertNotNull(encoded);
        assertNotEquals(raw, encoded);

        assertTrue(util.matches(raw, encoded));
    }
}
