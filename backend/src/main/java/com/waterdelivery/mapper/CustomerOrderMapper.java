package com.waterdelivery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.waterdelivery.entity.CustomerOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CustomerOrderMapper extends BaseMapper<CustomerOrder> {
}
