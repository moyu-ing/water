package com.waterdelivery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.waterdelivery.common.BizException;
import com.waterdelivery.dto.CouponDistributeRequest;
import com.waterdelivery.dto.CouponTemplateRequest;
import com.waterdelivery.entity.CouponTemplate;
import com.waterdelivery.entity.UserCoupon;
import com.waterdelivery.mapper.CouponTemplateMapper;
import com.waterdelivery.mapper.UserCouponMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 优惠券模块测试 — 覆盖 TC-COUPON
 */
@ExtendWith(MockitoExtension.class)
public class CouponServiceTest {

    @Mock private CouponTemplateMapper couponTemplateMapper;
    @Mock private UserCouponMapper userCouponMapper;

    @InjectMocks
    private CouponService couponService;

    // ==================== TC-COUPON-01: 获取优惠券模板列表 ====================
    @Test
    void testListTemplates() {
        CouponTemplate t1 = new CouponTemplate();
        t1.setId(1L);
        t1.setName("满100减10");
        t1.setType("FULL_REDUCTION");
        t1.setDiscountValue(new BigDecimal("10.00"));
        t1.setMinAmount(new BigDecimal("100.00"));

        CouponTemplate t2 = new CouponTemplate();
        t2.setId(2L);
        t2.setName("9折券");
        t2.setType("DISCOUNT");
        t2.setDiscountValue(new BigDecimal("10.0"));
        t2.setMinAmount(new BigDecimal("0.00"));

        when(couponTemplateMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(t1, t2));

        List<CouponTemplate> result = couponService.listTemplates();
        assertEquals(2, result.size());
        assertEquals("满100减10", result.get(0).getName());
        assertEquals("9折券", result.get(1).getName());
    }

    // ==================== TC-COUPON-02: 创建优惠券模板 ====================
    @Test
    void testSaveTemplate() {
        CouponTemplateRequest request = new CouponTemplateRequest();
        request.setName("满50减5");
        request.setType("FULL_REDUCTION");
        request.setDiscountValue(new BigDecimal("5.00"));
        request.setMinAmount(new BigDecimal("50.00"));
        request.setTotalQuantity(100);
        request.setValidDays(30);
        request.setStatus(1);

        when(couponTemplateMapper.insert(any(CouponTemplate.class))).thenReturn(1);

        couponService.saveTemplate(request);

        ArgumentCaptor<CouponTemplate> captor = ArgumentCaptor.forClass(CouponTemplate.class);
        verify(couponTemplateMapper).insert(captor.capture());
        CouponTemplate saved = captor.getValue();
        assertEquals("满50减5", saved.getName());
        assertEquals("FULL_REDUCTION", saved.getType());
        assertEquals(0, saved.getReceivedQuantity());
        assertNotNull(saved.getCreateTime());
    }

    // ==================== TC-COUPON-03: 删除优惠券模板 ====================
    @Test
    void testDeleteTemplate() {
        when(couponTemplateMapper.deleteById(1L)).thenReturn(1);
        couponService.deleteTemplate(1L);
        verify(couponTemplateMapper).deleteById(1L);
    }

    // ==================== TC-COUPON-04: 发放优惠券 ====================
    @Test
    void testDistributeCoupon() {
        CouponTemplate template = new CouponTemplate();
        template.setId(1L);
        template.setName("满100减10");
        template.setTotalQuantity(100);
        template.setReceivedQuantity(10);
        template.setValidDays(30);

        CouponDistributeRequest request = new CouponDistributeRequest();
        request.setUserIds(List.of(1L, 2L, 3L));

        when(couponTemplateMapper.selectById(1L)).thenReturn(template);
        when(userCouponMapper.insert(any(UserCoupon.class))).thenReturn(1);
        when(couponTemplateMapper.updateById(any(CouponTemplate.class))).thenReturn(1);

        couponService.distribute(1L, request);

        // 验证插入了3条用户优惠券
        ArgumentCaptor<UserCoupon> captor = ArgumentCaptor.forClass(UserCoupon.class);
        verify(userCouponMapper, times(3)).insert(captor.capture());
        List<UserCoupon> saved = captor.getAllValues();
        assertEquals(3, saved.size());
        for (UserCoupon uc : saved) {
            assertEquals(1L, uc.getTemplateId());
            assertEquals("UNUSED", uc.getStatus());
            assertNotNull(uc.getReceiveTime());
            assertNotNull(uc.getExpireTime());
        }
        // 验证模板已领取数量增加
        assertEquals(13, template.getReceivedQuantity());
    }

    // ==================== TC-COUPON-05: 发放优惠券 — 库存不足 ====================
    @Test
    void testDistributeCouponInsufficientStock() {
        CouponTemplate template = new CouponTemplate();
        template.setId(1L);
        template.setTotalQuantity(10);
        template.setReceivedQuantity(9); // 只剩1张

        CouponDistributeRequest request = new CouponDistributeRequest();
        request.setUserIds(List.of(1L, 2L, 3L)); // 需要3张

        when(couponTemplateMapper.selectById(1L)).thenReturn(template);

        BizException ex = assertThrows(BizException.class, () -> couponService.distribute(1L, request));
        assertTrue(ex.getMessage().contains("优惠券库存不足"));
    }

    // ==================== TC-COUPON-06: 用户领取优惠券成功 ====================
    @Test
    void testReceiveCouponSuccess() {
        CouponTemplate template = new CouponTemplate();
        template.setId(1L);
        template.setName("满50减5");
        template.setStatus(1);
        template.setTotalQuantity(100);
        template.setReceivedQuantity(10);
        template.setValidDays(30);

        when(couponTemplateMapper.selectById(1L)).thenReturn(template);
        when(userCouponMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(userCouponMapper.insert(any(UserCoupon.class))).thenReturn(1);
        when(couponTemplateMapper.updateById(any(CouponTemplate.class))).thenReturn(1);

        couponService.receiveCoupon(1L, 1L);

        ArgumentCaptor<UserCoupon> captor = ArgumentCaptor.forClass(UserCoupon.class);
        verify(userCouponMapper).insert(captor.capture());
        UserCoupon uc = captor.getValue();
        assertEquals(1L, uc.getUserId());
        assertEquals(1L, uc.getTemplateId());
        assertEquals("UNUSED", uc.getStatus());
    }

    // ==================== TC-COUPON-07: 用户领取优惠券 — 已领取过 ====================
    @Test
    void testReceiveCouponAlreadyReceived() {
        CouponTemplate template = new CouponTemplate();
        template.setId(1L);
        template.setStatus(1);
        template.setValidDays(30);

        when(couponTemplateMapper.selectById(1L)).thenReturn(template);
        when(userCouponMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        BizException ex = assertThrows(BizException.class, () -> couponService.receiveCoupon(1L, 1L));
        assertEquals("您已领取过该优惠券", ex.getMessage());
    }

    // ==================== TC-COUPON-08: 验证并使用优惠券 — 满减型 ====================
    @Test
    void testValidateAndUseCouponFullReduction() {
        UserCoupon uc = new UserCoupon();
        uc.setId(100L);
        uc.setUserId(1L);
        uc.setTemplateId(1L);
        uc.setStatus("UNUSED");
        uc.setExpireTime(LocalDateTime.now().plusDays(30));

        CouponTemplate template = new CouponTemplate();
        template.setId(1L);
        template.setName("满100减10");
        template.setType("FULL_REDUCTION");
        template.setDiscountValue(new BigDecimal("10.00"));
        template.setMinAmount(new BigDecimal("100.00"));

        when(userCouponMapper.selectById(100L)).thenReturn(uc);
        when(couponTemplateMapper.selectById(1L)).thenReturn(template);

        BigDecimal discount = couponService.validateAndUseCoupon(100L, new BigDecimal("150.00"));
        assertEquals(new BigDecimal("10.00"), discount);
    }

    // ==================== TC-COUPON-09: 验证并使用优惠券 — 折扣型 ====================
    @Test
    void testValidateAndUseCouponDiscount() {
        UserCoupon uc = new UserCoupon();
        uc.setId(100L);
        uc.setUserId(1L);
        uc.setTemplateId(1L);
        uc.setStatus("UNUSED");
        uc.setExpireTime(LocalDateTime.now().plusDays(30));

        CouponTemplate template = new CouponTemplate();
        template.setId(1L);
        template.setName("9折券");
        template.setType("DISCOUNT");
        template.setDiscountValue(new BigDecimal("10")); // 10% off = 9折
        template.setMinAmount(new BigDecimal("100.00"));

        when(userCouponMapper.selectById(100L)).thenReturn(uc);
        when(couponTemplateMapper.selectById(1L)).thenReturn(template);

        BigDecimal discount = couponService.validateAndUseCoupon(100L, new BigDecimal("150.00"));
        assertEquals(0, discount.compareTo(new BigDecimal("15.00"))); // 150 * 10% = 15
    }

    // ==================== TC-COUPON-10: 验证并使用优惠券 — 不满足门槛 ====================
    @Test
    void testValidateAndUseCouponBelowMinAmount() {
        UserCoupon uc = new UserCoupon();
        uc.setId(100L);
        uc.setUserId(1L);
        uc.setTemplateId(1L);
        uc.setStatus("UNUSED");
        uc.setExpireTime(LocalDateTime.now().plusDays(30));

        CouponTemplate template = new CouponTemplate();
        template.setId(1L);
        template.setName("满100减10");
        template.setType("FULL_REDUCTION");
        template.setDiscountValue(new BigDecimal("10.00"));
        template.setMinAmount(new BigDecimal("100.00"));

        when(userCouponMapper.selectById(100L)).thenReturn(uc);
        when(couponTemplateMapper.selectById(1L)).thenReturn(template);

        BizException ex = assertThrows(BizException.class, () ->
                couponService.validateAndUseCoupon(100L, new BigDecimal("50.00")));
        assertTrue(ex.getMessage().contains("订单金额未达到优惠券使用门槛"));
    }

    // ==================== TC-COUPON-11: 验证并使用优惠券 — 已过期 ====================
    @Test
    void testValidateAndUseCouponExpired() {
        UserCoupon uc = new UserCoupon();
        uc.setId(100L);
        uc.setUserId(1L);
        uc.setTemplateId(1L);
        uc.setStatus("UNUSED");
        uc.setExpireTime(LocalDateTime.now().minusDays(1)); // 已过期

        when(userCouponMapper.selectById(100L)).thenReturn(uc);
        when(userCouponMapper.updateById(any(UserCoupon.class))).thenReturn(1);

        BizException ex = assertThrows(BizException.class, () ->
                couponService.validateAndUseCoupon(100L, new BigDecimal("100.00")));
        assertEquals("优惠券已过期", ex.getMessage());
        assertEquals("EXPIRED", uc.getStatus());
    }

    // ==================== TC-COUPON-12: 标记优惠券已使用 ====================
    @Test
    void testMarkCouponUsed() {
        UserCoupon uc = new UserCoupon();
        uc.setId(100L);
        uc.setStatus("UNUSED");

        when(userCouponMapper.selectById(100L)).thenReturn(uc);
        when(userCouponMapper.updateById(any(UserCoupon.class))).thenReturn(1);

        couponService.markCouponUsed(100L, 200L);

        assertEquals("USED", uc.getStatus());
        assertEquals(200L, uc.getOrderId());
        assertNotNull(uc.getUseTime());
    }

    // ==================== TC-COUPON-13: 用户优惠券列表 ====================
    @Test
    void testListUserCoupons() {
        UserCoupon uc = new UserCoupon();
        uc.setId(1L);
        uc.setUserId(1L);
        uc.setTemplateId(1L);
        uc.setStatus("UNUSED");
        uc.setExpireTime(LocalDateTime.now().plusDays(30));
        uc.setReceiveTime(LocalDateTime.now());

        CouponTemplate template = new CouponTemplate();
        template.setId(1L);
        template.setName("满100减10");
        template.setType("FULL_REDUCTION");
        template.setDiscountValue(new BigDecimal("10.00"));
        template.setMinAmount(new BigDecimal("100.00"));

        when(userCouponMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(uc));
        when(couponTemplateMapper.selectBatchIds(anyList())).thenReturn(List.of(template));

        List<Map<String, Object>> result = couponService.listUserCoupons(1L);
        assertEquals(1, result.size());
        assertEquals("满100减10", result.get(0).get("name"));
        assertEquals("UNUSED", result.get(0).get("status"));
    }
}
