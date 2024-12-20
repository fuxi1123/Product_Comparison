package com.example.product_comparison.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ScrapeController {

    private final RestTemplate restTemplate;

    // Python 爬虫服务的 URL
    @Value("${scraper.service.url:http://127.0.0.1:5000}")
    private String scraperServiceUrl;

    public ScrapeController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 根据商品名爬取淘宝商品信息
     *
     * @param query 商品名称
     * @return 爬取到的商品信息列表，附加 category
     */
    @GetMapping("/scrape/taobao")
    public ResponseEntity<?> scrapeTaobao(@RequestParam String query) {
        // 拼接爬虫服务的 URL
        String url = scraperServiceUrl + "/scrape?query=" + query;

        try {
            // 调用 Python 爬虫服务
            ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);
            List<Map<String, Object>> products = response.getBody();

            // 遍历结果，添加 "category" 字段
            if (products != null) {
                for (Map<String, Object> product : products) {
                    product.put("category", query);
                }
            }

            // 返回爬取结果
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            // 错误处理，使用 HashMap 手动创建返回的 JSON 格式
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch data from scraper service");
            errorResponse.put("details", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
