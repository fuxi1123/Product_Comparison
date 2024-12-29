package com.example.product_comparison.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.product_comparison.entity.PriceAlert;
import com.example.product_comparison.entity.Product;
import com.example.product_comparison.mapper.PriceAlertMapper;
import com.example.product_comparison.mapper.ProductMapper;
import com.example.product_comparison.utils.Result;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/alert")
@CrossOrigin
public class AlertController {

    private final PriceAlertMapper priceAlertMapper;
    private final ProductMapper productMapper;

    public AlertController(PriceAlertMapper priceAlertMapper, ProductMapper productMapper) {
        this.priceAlertMapper = priceAlertMapper;
        this.productMapper = productMapper;
    }

    @PostMapping
    public Result setPriceAlert(@RequestBody PriceAlert priceAlert) {
        try {
            // 查询当前最大 ID
            Integer maxId = priceAlertMapper.getMaxId();
            if (maxId == null) {
                maxId = 0; // 如果表为空，从 0 开始
            }

            // 设置手动自增的 ID
            priceAlert.setId(maxId + 1);

            // 获取当前价格并设置
            Double currentPrice = Double.valueOf(productMapper.selectById(priceAlert.getProductId()).getPrice());
            priceAlert.setInitialPrice(currentPrice);

            // 插入数据库
            priceAlertMapper.insert(priceAlert);

            return Result.ok().message("Price alert set successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error().message("Failed to set price alert: " + e.getMessage());
        }
    }



    @DeleteMapping("/{id}")
    public Result deletePriceAlert(@PathVariable Integer id) {
        try {
            int rowsAffected = priceAlertMapper.deleteById(id);
            if (rowsAffected == 0) {
                return Result.error().message("Price alert not found or already deleted.");
            }
            return Result.ok().message("Price alert deleted successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error().message("Failed to delete price alert: " + e.getMessage());
        }
    }


    @GetMapping("/getalert/{userId}")
    public Result getAlertsByUser(@PathVariable Long userId) {
        try {
            // 查询用户的所有提醒
            System.out.println("aaa");
            List<PriceAlert> alerts = priceAlertMapper.selectList(
                    new QueryWrapper<PriceAlert>().eq("user_id", userId)
            );

            // 查询每个提醒对应的商品信息
            List<Map<String, Object>> alertDetails = new ArrayList<>();
            for (PriceAlert alert : alerts) {
                Product product = productMapper.selectById(alert.getProductId());
                Map<String, Object> detail = new HashMap<>();
                detail.put("alert", alert);
                detail.put("product", product);
                alertDetails.add(detail);
            }

            // 返回成功结果
            return Result.ok().data("alerts", alertDetails);
        } catch (Exception e) {
            // 返回错误结果
            return Result.error().message("Failed to fetch user alerts").data("details", e.getMessage());
        }
    }

}
