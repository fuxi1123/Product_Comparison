package com.example.product_comparison.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.product_comparison.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
