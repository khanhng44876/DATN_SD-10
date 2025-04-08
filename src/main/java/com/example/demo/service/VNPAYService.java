package com.example.demo.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VNPAYService {
    @Value("${vnpay.tmnCode}")
    private String vnp_TmnCode;

    @Value("${vnpay.hashSecret}")
    private String vnp_HashSecret;

    @Value("${vnpay.url}")
    private String vnp_Url;

    @Value("${vnpay.returnUrl}")
    private String vnp_ReturnUrl;

    public String createOrder(HttpServletRequest request, int amount, String orderInfo, String baseUrl) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_OrderType = "other";
        String vnp_Amount = String.valueOf(amount * 100); // Nhân 100 để khử thập phân
        String vnp_IpAddr = request.getRemoteAddr();
        String vnp_TxnRef = String.valueOf(System.currentTimeMillis());
        String vnp_CreateDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

        Map<String, String> vnp_Params = new TreeMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", vnp_Amount);
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", orderInfo);
        vnp_Params.put("vnp_OrderType", vnp_OrderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> entry : vnp_Params.entrySet()) {
            hashData.append(entry.getKey()).append("=")
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)).append("&");
        }
        hashData.deleteCharAt(hashData.length() - 1);
        String vnp_SecureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
        vnp_Params.put("vnp_SecureHash", vnp_SecureHash);

        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> entry : vnp_Params.entrySet()) {
            query.append(entry.getKey()).append("=")
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)).append("&");
        }
        query.deleteCharAt(query.length() - 1);
        return vnp_Url + "?" + query.toString();
    }

    public int orderReturn(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        for (String key : request.getParameterMap().keySet()) {
            String value = request.getParameter(key);
            if (value != null && !value.isEmpty()) {
                fields.put(key, value);
            }
        }

        String vnp_SecureHash = fields.get("vnp_SecureHash");
        fields.remove("vnp_SecureHash");

        String signValue = hashAllFields(fields);
        if (signValue.equals(vnp_SecureHash)) {
            return "00".equals(fields.get("vnp_ResponseCode")) ? 1 : 0; // 1: Thành công, 0: Thất bại
        }
        return -1; // Sai chữ ký
    }

    private String hmacSHA512(String key, String data) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA512");
            mac.init(new javax.crypto.spec.SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error while hashing", e);
        }
    }

    private String hashAllFields(Map<String, String> fields) {
        StringBuilder sb = new StringBuilder();
        TreeMap<String, String> sortedFields = new TreeMap<>(fields);
        for (Map.Entry<String, String> entry : sortedFields.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        sb.deleteCharAt(sb.length() - 1);
        return hmacSHA512(vnp_HashSecret, sb.toString());
    }
}
