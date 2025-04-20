package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VNPayConfig {
    @Value("${vnpay.tmn-code}")
    public String vnp_TmnCode;

    @Value("${vnpay.hash-secret}")
    public String vnp_HashSecret;

    @Value("${vnpay.url}")
    public String vnp_Url;

    @Value("${vnpay.return-url}")
    public String vnp_ReturnUrl;
}
