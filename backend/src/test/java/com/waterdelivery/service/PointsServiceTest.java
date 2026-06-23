package com.waterdelivery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.waterdelivery.common.BizException;
import com.waterdelivery.entity.AppUser;
import com.waterdelivery.entity.UserPoints;
import com.waterdelivery.mapper.AppUserMapper;
import com.waterdelivery.mapper.UserPointsMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 积分模块测试 — 覆盖 TC-POINTS
 */
@ExtendWith(MockitoExtension.class)
public class PointsServiceTest {

    @Mock private UserPointsMapper userPointsMapper;
    @Mock private AppUserMapper appUserMapper;

    @InjectMocks
    private PointsService pointsService;

    // ==================== TC-POINTS-01: 获取积分余额 ====================
    @Test
    void testGetBalance() {
        AppUser user = new AppUser();
        user.setId(1L);
        user.setPoints(500);

        when(appUserMapper.selectById(1L)).thenReturn(user);

        int balance = pointsService.getBalance(1L);
        assertEquals(500, balance);
    }

    // ==================== TC-POINTS-02: 获取积分余额 — 用户不存在 ====================
    @Test
    void testGetBalanceUserNotFound() {
        when(appUserMapper.selectById(999L)).thenReturn(null);

        int balance = pointsService.getBalance(999L);
        assertEquals(0, balance);
    }

    // ==================== TC-POINTS-03: 获取积分历史记录 ====================
    @Test
    void testGetHistory() {
        UserPoints up1 = new UserPoints();
        up1.setId(1L);
        up1.setUserId(1L);
        up1.setPoints(100);
        up1.setSource("ORDER");
        up1.setSourceDesc("订单完成赠送积分");

        when(userPointsMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(up1));

        List<UserPoints> history = pointsService.getHistory(1L);
        assertEquals(1, history.size());
        assertEquals(100, history.get(0).getPoints());
        assertEquals("ORDER", history.get(0).getSource());
    }

    // ==================== TC-POINTS-04: 订单完成获取积分 ====================
    @Test
    void testEarnPoints() {
        AppUser user = new AppUser();
        user.setId(1L);
        user.setPoints(500);

        when(userPointsMapper.insert(any(UserPoints.class))).thenReturn(1);
        when(appUserMapper.selectById(1L)).thenReturn(user);
        when(appUserMapper.updateById(any(AppUser.class))).thenReturn(1);

        pointsService.earnPoints(1L, 50, "ORDER", "订单完成赠送积分");

        // 验证积分流水记录
        ArgumentCaptor<UserPoints> captor = ArgumentCaptor.forClass(UserPoints.class);
        verify(userPointsMapper).insert(captor.capture());
        UserPoints record = captor.getValue();
        assertEquals(50, record.getPoints());
        assertEquals("ORDER", record.getSource());
        assertEquals("订单完成赠送积分", record.getSourceDesc());

        // 验证用户积分更新
        assertEquals(550, user.getPoints()); // 500 + 50
        verify(appUserMapper).updateById(user);
    }

    // ==================== TC-POINTS-05: 下单抵扣积分 ====================
    @Test
    void testDeductPoints() {
        AppUser user = new AppUser();
        user.setId(1L);
        user.setPoints(500);

        when(userPointsMapper.insert(any(UserPoints.class))).thenReturn(1);
        when(appUserMapper.selectById(1L)).thenReturn(user);
        when(appUserMapper.updateById(any(AppUser.class))).thenReturn(1);

        pointsService.deductPoints(1L, 200);

        // 验证积分流水记录（负数）
        ArgumentCaptor<UserPoints> captor = ArgumentCaptor.forClass(UserPoints.class);
        verify(userPointsMapper).insert(captor.capture());
        UserPoints record = captor.getValue();
        assertEquals(-200, record.getPoints());
        assertEquals("EXCHANGE", record.getSource());
        assertEquals("下单抵扣", record.getSourceDesc());

        // 验证用户积分更新
        assertEquals(300, user.getPoints()); // 500 - 200
    }

    // ==================== TC-POINTS-06: 积分不足时扣减失败 ====================
    @Test
    void testDeductPointsInsufficient() {
        AppUser user = new AppUser();
        user.setId(1L);
        user.setPoints(50); // 只有50分

        when(appUserMapper.selectById(1L)).thenReturn(user);

        BizException ex = assertThrows(BizException.class, () -> pointsService.deductPoints(1L, 100));
        assertEquals("积分余额不足", ex.getMessage());
        verify(userPointsMapper, never()).insert(any());
    }

    // ==================== TC-POINTS-07: 积分换算金额（100积分=1元） ====================
    @Test
    void testPointsToAmount() {
        BigDecimal amount = PointsService.pointsToAmount(100);
        assertEquals(new BigDecimal("1.00"), amount);

        BigDecimal amount2 = PointsService.pointsToAmount(150);
        assertEquals(new BigDecimal("1.50"), amount2);

        BigDecimal amount3 = PointsService.pointsToAmount(0);
        assertEquals(new BigDecimal("0.00"), amount3);
    }

    // ==================== TC-POINTS-08: earnPoints 积分<=0 时直接返回 ====================
    @Test
    void testEarnPointsZeroOrNegative() {
        pointsService.earnPoints(1L, 0, "ORDER", "test");
        verify(userPointsMapper, never()).insert(any());
        verify(appUserMapper, never()).selectById(anyLong());

        pointsService.earnPoints(1L, -5, "ORDER", "test");
        verify(userPointsMapper, never()).insert(any());
    }
}
