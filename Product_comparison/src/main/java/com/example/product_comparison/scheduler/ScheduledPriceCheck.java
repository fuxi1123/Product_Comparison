package com.example.product_comparison.scheduler;

import com.example.product_comparison.entity.PriceAlert;
import com.example.product_comparison.entity.Product;
import com.example.product_comparison.mapper.PriceAlertMapper;
import com.example.product_comparison.mapper.ProductMapper;
import com.example.product_comparison.utils.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ScheduledPriceCheck {

    private final ProductMapper productMapper;
    private final PriceAlertMapper priceAlertMapper;
    private final EmailService emailService;
    private final RestTemplate restTemplate;

    @Value("${scraper.service.url:http://127.0.0.1:5000}")
    private String scraperServiceUrl;

    public ScheduledPriceCheck(ProductMapper productMapper, PriceAlertMapper priceAlertMapper, EmailService emailService, RestTemplate restTemplate) {
        this.productMapper = productMapper;
        this.priceAlertMapper = priceAlertMapper;
        this.emailService = emailService;
        this.restTemplate = restTemplate;
    }

    @Scheduled(fixedRate = 86400000) // 每天运行一次
    public void checkPriceAlerts() {
        // 获取所有有提醒的商品ID
        List<Long> productIds = priceAlertMapper.getProductIdsFromAlerts();

        for (Long productId : productIds) {
            Product product = productMapper.selectById(productId);
            if (product == null) continue;

            try {
                // 调用爬虫服务，获取最新价格
                String url = scraperServiceUrl + "/scrape_by_title?title=" + product.getTitle() + "&source=" + product.getSource();
                Map<String, Object> scrapedProduct = restTemplate.getForObject(url, HashMap.class);

                // 如果爬虫服务未返回数据，则跳过
                if (scrapedProduct == null || scrapedProduct.get("price") == null) {
                    System.err.println("Failed to fetch price for product: " + product.getTitle());
                    continue;
                }

                // 获取新价格
                Double newPrice = Double.valueOf(scrapedProduct.get("price").toString());
                System.out.println("New price for product " + product.getTitle() + ": " + newPrice);

                // 检查价格是否变化
                if (newPrice<(Double.valueOf(product.getPrice()))) {
                    System.out.println("Price changed for product: " + product.getTitle());
                    product.setPrice(String.valueOf(newPrice));
                    productMapper.updateById(product); // 更新数据库中的商品价格

                    // 获取所有该商品的提醒
                    List<PriceAlert> alerts = priceAlertMapper.getAlertsForProduct(product.getId());
                    for (PriceAlert alert : alerts) {
                        // 发送价格变动提醒邮件
                        String subject = "Price Change Alert!";
                        String message = "The product \"" + product.getTitle() + "\" has a new price: ¥" + newPrice +
                                " (was: ¥" + alert.getInitialPrice() + "). Check it here: " + product.getLink();
                        emailService.sendEmail(alert.getEmail(), subject, message);

                        // 更新提醒中的初始价格
                        alert.setInitialPrice(newPrice);
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to scrape price for product: " + product.getTitle() + ". Error: " + e.getMessage());
            }
        }
    }
}
