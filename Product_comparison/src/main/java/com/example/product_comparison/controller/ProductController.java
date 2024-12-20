package com.example.product_comparison.controller;

import com.example.product_comparison.entity.Product;
import com.example.product_comparison.mapper.ProductMapper;
import com.example.product_comparison.utils.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product")
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
     * 根据商品名称爬取淘宝商品信息，并保存到数据库
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

            List<Map<String, String>> enrichedProducts = new ArrayList<>();
            if (products != null) {
                for (Map<String, String> productData : products) {
                    // 保存到数据库
                    Product product = new Product();
                    product.setTitle(productData.get("title"));
                    product.setPrice(productData.get("price"));
                    product.setLink(productData.get("link"));
                    product.setImage(productData.get("image"));
                    product.setCategory(query);
                    product.setSource("淘宝"); // 设置来源为 "淘宝"
                    productMapper.insert(product);

                    // 增加 source 字段返回前端
                    productData.put("source", "淘宝");
                    enrichedProducts.add(productData);
                }
            }
            return Result.ok().data("products", enrichedProducts);
        } catch (Exception e) {
            return Result.error().message("Failed to scrape products").data("details", e.getMessage());
        }
    }
}
