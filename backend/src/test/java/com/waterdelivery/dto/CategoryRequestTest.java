package com.waterdelivery.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CategoryRequestTest {

    @Test
    void testGetterSetter() {
        CategoryRequest request = new CategoryRequest();

        request.setName("桶装水");
        request.setSortNum(1);

        assertEquals("桶装水", request.getName());
        assertEquals(1, request.getSortNum());
    }
}