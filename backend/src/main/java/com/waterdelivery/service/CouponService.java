package com.waterdelivery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.waterdelivery.common.BizException;
import com.waterdelivery.dto.CouponDistributeRequest;
import com.waterdelivery.dto.CouponTemplateRequest;
import com.waterdelivery.entity.CouponTemplate;
import com.waterdelivery.entity.UserCoupon;
import com.waterdelivery.mapper.CouponTemplateMapper;
import com.waterdelivery.mapper.UserCouponMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CouponService {

    private final CouponTemplateMapper couponTemplateMapper;
    private final UserCouponMapper userCouponMapper;

    public CouponService(CouponTemplateMapper couponTemplateMapper,
                        UserCouponMapper userCouponMapper) {
        this.couponTemplateMapper = couponTemplateMapper;
        this.userCouponMapper = userCouponMapper;
    }

    // ==================== 模板管理 ====================
    public List<CouponTemplate> listTemplates() {
        return couponTemplateMapper.selectList(new LambdaQueryWrapper<CouponTemplate>()
                .orderByDesc(CouponTemplate::getId));
    }

    public void saveTemplate(CouponTemplateRequest request) {
        CouponTemplate template = new CouponTemplate();
        fillTemplate(template, request);
        template.setReceivedQuantity(0);
        template.setCreateTime(LocalDateTime.now());
        template.setUpdateTime(LocalDateTime.now());
        couponTemplateMapper.insert(template);
    }

    public void updateTemplate(Long id, CouponTemplateRequest request) {
        CouponTemplate template = couponTemplateMapper.selectById(id);
        if (template == null) {
            throw new BizException("优惠券模板不存在");
        }
        fillTemplate(template, request);
        template.setUpdateTime(LocalDateTime.now());
        couponTemplateMapper.updateById(template);
    }

    public void deleteTemplate(Long id) {
        couponTemplateMapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void distribute(Long templateId, CouponDistributeRequest request) {
        CouponTemplate template = couponTemplateMapper.selectById(templateId);
        if (template == null) {
            throw new BizException("优惠券模板不存在");
        }
        if (template.getTotalQuantity() != null && template.getTotalQuantity() > 0) {
            int remaining = template.getTotalQuantity() - template.getReceivedQuantity();
            if (request.getUserIds().size() > remaining) {
                throw new BizException("优惠券库存不足，剩余 " + remaining + " 张");
            }
        }
        for (Long userId : request.getUserIds()) {
            UserCoupon coupon = new UserCoupon();
            coupon.setUserId(userId);
            coupon.setTemplateId(templateId);
            coupon.setStatus("UNUSED");
            coupon.setReceiveTime(LocalDateTime.now());
            coupon.setExpireTime(LocalDateTime.now().plusDays(template.getValidDays()));
            coupon.setCreateTime(LocalDateTime.now());
            coupon.setUpdateTime(LocalDateTime.now());
            userCouponMapper.insert(coupon);
        }
        template.setReceivedQuantity(template.getReceivedQuantity() + request.getUserIds().size());
        template.setUpdateTime(LocalDateTime.now());
        couponTemplateMapper.updateById(template);
    }

    // ==================== 用户端 ====================
    public List<Map<String, Object>> listUserCoupons(Long userId) {
        List<UserCoupon> coupons = userCouponMapper.selectList(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .orderByDesc(UserCoupon::getId));
        if (coupons.isEmpty()) {
            return Collections.emptyList();
        }
        // 惰性过期检查
        for (UserCoupon uc : coupons) {
            if ("UNUSED".equals(uc.getStatus()) && uc.getExpireTime() != null
                    && uc.getExpireTime().isBefore(LocalDateTime.now())) {
                uc.setStatus("EXPIRED");
                uc.setUpdateTime(LocalDateTime.now());
                userCouponMapper.updateById(uc);
            }
        }
        Map<Long, CouponTemplate> templateMap = couponTemplateMapper.selectBatchIds(
                coupons.stream().map(UserCoupon::getTemplateId).distinct().toList())
                .stream().collect(Collectors.toMap(CouponTemplate::getId, t -> t));
        return coupons.stream().map(uc -> {
            Map<String, Object> map = new HashMap<>();
            CouponTemplate t = templateMap.get(uc.getTemplateId());
            map.put("id", uc.getId());
            map.put("templateId", uc.getTemplateId());
            map.put("status", uc.getStatus());
            map.put("expireTime", uc.getExpireTime());
            map.put("receiveTime", uc.getReceiveTime());
            if (t != null) {
                map.put("name", t.getName());
                map.put("type", t.getType());
                map.put("discountValue", t.getDiscountValue());
                map.put("minAmount", t.getMinAmount());
            }
            return map;
        }).toList();
    }

    public List<Map<String, Object>> listAvailableCoupons(Long userId) {
        return listUserCoupons(userId).stream()
                .filter(c -> "UNUSED".equals(c.get("status")))
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public void receiveCoupon(Long userId, Long templateId) {
        CouponTemplate template = couponTemplateMapper.selectById(templateId);
        if (template == null || template.getStatus() != 1) {
            throw new BizException("优惠券不存在或已下架");
        }
        if (template.getTotalQuantity() != null && template.getTotalQuantity() > 0
                && template.getReceivedQuantity() >= template.getTotalQuantity()) {
            throw new BizException("优惠券已领完");
        }
        // 检查是否已领取
        Long count = userCouponMapper.selectCount(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getTemplateId, templateId));
        if (count > 0) {
            throw new BizException("您已领取过该优惠券");
        }
        UserCoupon coupon = new UserCoupon();
        coupon.setUserId(userId);
        coupon.setTemplateId(templateId);
        coupon.setStatus("UNUSED");
        coupon.setReceiveTime(LocalDateTime.now());
        coupon.setExpireTime(LocalDateTime.now().plusDays(template.getValidDays()));
        coupon.setCreateTime(LocalDateTime.now());
        coupon.setUpdateTime(LocalDateTime.now());
        userCouponMapper.insert(coupon);
        template.setReceivedQuantity(template.getReceivedQuantity() + 1);
        template.setUpdateTime(LocalDateTime.now());
        couponTemplateMapper.updateById(template);
    }

    /**
     * 验证并使用优惠券，返回抵扣金额
     */
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal validateAndUseCoupon(Long userCouponId, BigDecimal orderAmount) {
        UserCoupon uc = userCouponMapper.selectById(userCouponId);
        if (uc == null || !"UNUSED".equals(uc.getStatus())) {
            throw new BizException("优惠券不可用");
        }
        if (uc.getExpireTime() != null && uc.getExpireTime().isBefore(LocalDateTime.now())) {
            uc.setStatus("EXPIRED");
            uc.setUpdateTime(LocalDateTime.now());
            userCouponMapper.updateById(uc);
            throw new BizException("优惠券已过期");
        }
        CouponTemplate template = couponTemplateMapper.selectById(uc.getTemplateId());
        if (template == null) {
            throw new BizException("优惠券模板不存在");
        }
        if (orderAmount.compareTo(template.getMinAmount()) < 0) {
            throw new BizException("订单金额未达到优惠券使用门槛（满¥" + template.getMinAmount() + "可用）");
        }
        BigDecimal discount;
        if ("FULL_REDUCTION".equals(template.getType())) {
            discount = template.getDiscountValue();
        } else if ("DISCOUNT".equals(template.getType())) {
            // discountValue = 10 表示9折 -> 折扣 = 原价 * 10%
            discount = orderAmount.multiply(
                    template.getDiscountValue().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        } else {
            throw new BizException("未知的优惠券类型");
        }
        return discount.min(orderAmount);
    }

    @Transactional(rollbackFor = Exception.class)
    public void markCouponUsed(Long userCouponId, Long orderId) {
        UserCoupon uc = userCouponMapper.selectById(userCouponId);
        if (uc != null) {
            uc.setStatus("USED");
            uc.setUseTime(LocalDateTime.now());
            uc.setOrderId(orderId);
            uc.setUpdateTime(LocalDateTime.now());
            userCouponMapper.updateById(uc);
        }
    }

    private void fillTemplate(CouponTemplate template, CouponTemplateRequest request) {
        template.setName(request.getName());
        template.setType(request.getType());
        template.setDiscountValue(request.getDiscountValue());
        template.setMinAmount(request.getMinAmount());
        template.setTotalQuantity(request.getTotalQuantity());
        template.setValidDays(request.getValidDays());
        template.setStatus(request.getStatus());
    }
}
