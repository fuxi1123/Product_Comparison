package com.example.product_comparison;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.product_comparison.mapper")
public class ProductComparisonApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductComparisonApplication.class, args);
    }

}
