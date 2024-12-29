package com.example.product_comparison.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.product_comparison.entity.PriceAlert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PriceAlertMapper extends BaseMapper<PriceAlert> {

    @Select("SELECT DISTINCT product_id FROM price_alert")
    List<Long> getProductIdsFromAlerts();

    @Select("SELECT * FROM price_alert WHERE product_id = #{productId}")
    List<PriceAlert> getAlertsForProduct(Long productId);

    @Select("SELECT MAX(id) FROM price_alert")
    Integer getMaxId();
}
