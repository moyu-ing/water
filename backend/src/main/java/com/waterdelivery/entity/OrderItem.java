package com.waterdelivery.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_item")
public class OrderItem {
    private Long id;
    private Long orderId;
    private Long productId;
    private String productName;
    private String productImage;
    private String productSpec;
    private BigDecimal productPrice;
    private Integer quantity;
    private BigDecimal amount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
