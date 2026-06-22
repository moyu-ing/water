package com.waterdelivery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.waterdelivery.common.BizException;
import com.waterdelivery.entity.AppUser;
import com.waterdelivery.entity.UserPoints;
import com.waterdelivery.mapper.AppUserMapper;
import com.waterdelivery.mapper.UserPointsMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PointsService {

    private static final int POINTS_EXCHANGE_RATE = 100; // 100积分 = 1元

    private final UserPointsMapper userPointsMapper;
    private final AppUserMapper appUserMapper;

    public PointsService(UserPointsMapper userPointsMapper, AppUserMapper appUserMapper) {
        this.userPointsMapper = userPointsMapper;
        this.appUserMapper = appUserMapper;
    }

    public int getBalance(Long userId) {
        AppUser user = appUserMapper.selectById(userId);
        return user != null && user.getPoints() != null ? user.getPoints() : 0;
    }

    public List<UserPoints> getHistory(Long userId) {
        return userPointsMapper.selectList(new LambdaQueryWrapper<UserPoints>()
                .eq(UserPoints::getUserId, userId)
                .orderByDesc(UserPoints::getId));
    }

    @Transactional(rollbackFor = Exception.class)
    public void earnPoints(Long userId, int points, String source, String sourceDesc) {
        if (points <= 0) {
            return;
        }
        UserPoints record = new UserPoints();
        record.setUserId(userId);
        record.setPoints(points);
        record.setSource(source);
        record.setSourceDesc(sourceDesc);
        record.setCreateTime(LocalDateTime.now());
        userPointsMapper.insert(record);

        AppUser user = appUserMapper.selectById(userId);
        if (user != null) {
            int currentPoints = user.getPoints() != null ? user.getPoints() : 0;
            user.setPoints(currentPoints + points);
            user.setUpdateTime(LocalDateTime.now());
            appUserMapper.updateById(user);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deductPoints(Long userId, int points) {
        if (points <= 0) {
            return;
        }
        AppUser user = appUserMapper.selectById(userId);
        int currentPoints = user != null && user.getPoints() != null ? user.getPoints() : 0;
        if (user == null || currentPoints < points) {
            throw new BizException("积分余额不足");
        }
        UserPoints record = new UserPoints();
        record.setUserId(userId);
        record.setPoints(-points);
        record.setSource("EXCHANGE");
        record.setSourceDesc("下单抵扣");
        record.setCreateTime(LocalDateTime.now());
        userPointsMapper.insert(record);

        user.setPoints(currentPoints - points);
        user.setUpdateTime(LocalDateTime.now());
        appUserMapper.updateById(user);
    }

    /**
     * 将积分转换为可抵扣金额（100积分 = 1元）
     */
    public static java.math.BigDecimal pointsToAmount(int points) {
        return java.math.BigDecimal.valueOf(points).divide(
                java.math.BigDecimal.valueOf(POINTS_EXCHANGE_RATE), 2, java.math.RoundingMode.DOWN);
    }
}
