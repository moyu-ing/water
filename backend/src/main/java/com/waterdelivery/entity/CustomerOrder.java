package com.waterdelivery.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("customer_order")
public class CustomerOrder {
    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    private String status;
    private String payType;
    private String contactName;
    private String contactPhone;
    private String fullAddress;
    @TableField("remark")
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
