package com.waterdelivery.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_points")
public class UserPoints {
    private Long id;
    private Long userId;
    private Integer points;
    private String source;
    private String sourceDesc;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
}
