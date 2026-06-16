package com.waterdelivery.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("category")
public class Category {
    private Long id;
    private Long parentId;
    private String name;
    private String image;
    private Integer sortNum;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
