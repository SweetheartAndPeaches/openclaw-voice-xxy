package com.podcast.voice.config;

import com.stripe.Stripe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * Stripe 配置类
 * 初始化 Stripe API 密钥
 */
@Configuration
public class StripeConfig {

    @Value("${payment.stripe.secret-key}")
    private String secretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }
}