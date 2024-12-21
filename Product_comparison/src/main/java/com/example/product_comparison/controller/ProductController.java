package com.example.product_comparison.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.product_comparison.entity.Product;
import com.example.product_comparison.mapper.ProductMapper;
import com.example.product_comparison.utils.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.sql.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductMapper productMapper;
    private final RestTemplate restTemplate;

    @Value("${scraper.service.url:http://127.0.0.1:5000}")
    private String scraperServiceUrl;

    public ProductController(ProductMapper productMapper, RestTemplate restTemplate) {
        this.productMapper = productMapper;
        this.restTemplate = restTemplate;
    }

    /**
     * 根据商品名称爬取商品信息，并保存到数据库
     *
     * @param query 商品名称
     * @return 爬取的商品信息列表
     */
    @GetMapping("/scrape")
    public Result scrapeAndSaveProducts(@RequestParam String query) {
        String url = scraperServiceUrl + "/scrape?query=" + query;

        try {
            // 调用爬虫服务
            List<Map<String, String>> products = restTemplate.getForObject(url, List.class);

            List<Product> savedProducts = new ArrayList<>();
            if (products != null) {
                for (Map<String, String> productData : products) {
                    // 构建 Product 对象
                    Product product = new Product();
                    product.setTitle(productData.get("title"));
                    product.setPrice(productData.get("price"));
                    product.setLink(productData.get("link"));
                    product.setImage(productData.get("image"));
                    product.setCategory(query);
                    product.setSource(productData.get("source"));
                    product.setDate(new Date(System.currentTimeMillis()));

                    // 保存到数据库
                    productMapper.insert(product);
                    savedProducts.add(product);
                }
            }

            return Result.ok().data("products", savedProducts);
        } catch (Exception e) {
            return Result.error().message("Failed to scrape products").data("details", e.getMessage());
        }
    }

    @GetMapping("/history")
    public Result getProductHistory(@RequestParam String title) {
        try {
            QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("title", title).orderByAsc("date");
            List<Product> history = productMapper.selectList(queryWrapper);
            return Result.ok().data("history", history);
        } catch (Exception e) {
            return Result.error().message("Failed to fetch product history").data("details", e.getMessage());
        }
    }

}
