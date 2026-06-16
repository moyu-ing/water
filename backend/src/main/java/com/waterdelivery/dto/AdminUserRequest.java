package com.waterdelivery.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AdminUserRequest {
    private String username;
    private String password;
    private String nickname;
    private Integer status = 1;
    private List<Long> roleIds = new ArrayList<>();
}
