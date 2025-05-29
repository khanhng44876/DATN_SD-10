package com.example.demo.service;

import com.example.demo.config.VNPayConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VNPAYService {
    @Autowired
    private VNPayConfig vnPayConfig;

    public String createOrder(HttpServletRequest request, int amount, String orderInfo) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_OrderInfo = orderInfo;
        String vnp_OrderType = "billpayment";
        String vnp_TxnRef = String.valueOf(System.currentTimeMillis());
        String vnp_IpAddr = request.getRemoteAddr();
        String vnp_CreateDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String vnp_ExpireDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(System.currentTimeMillis() + 15 * 60 * 1000));

        Map<String, String> vnp_Params = new TreeMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnPayConfig.vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100)); // Nhân 100 để bỏ phần thập phân
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", vnp_OrderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> entry : vnp_Params.entrySet()) {
            hashData.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            hashData.append("=");
            hashData.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            hashData.append("&");
        }
        hashData.deleteCharAt(hashData.length() - 1);
        String vnp_SecureHash = hmacSHA512(vnPayConfig.vnp_HashSecret, hashData.toString());
        vnp_Params.put("vnp_SecureHash", vnp_SecureHash);

        StringBuilder vnpayUrl = new StringBuilder(vnPayConfig.vnp_Url);
        vnpayUrl.append("?");
        for (Map.Entry<String, String> entry : vnp_Params.entrySet()) {
            vnpayUrl.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            vnpayUrl.append("=");
            vnpayUrl.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            vnpayUrl.append("&");
        }
        vnpayUrl.deleteCharAt(vnpayUrl.length() - 1);
        return vnpayUrl.toString();
    }

    public int orderReturn(HttpServletRequest request) {
        Map<String, String> fields = new TreeMap<>();
        for (String key : request.getParameterMap().keySet()) {
            String value = request.getParameter(key);
            if (value != null && !value.isEmpty()) {
                fields.put(key, value);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        fields.remove("vnp_SecureHash");

        String signValue = "";
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            signValue += URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" +
                    URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8) + "&";
        }
        signValue = signValue.substring(0, signValue.length() - 1);
        String sign = hmacSHA512(vnPayConfig.vnp_HashSecret, signValue);

        if (sign.equals(vnp_SecureHash)) {
            return "00".equals(request.getParameter("vnp_ResponseCode")) ? 1 : 0;
        }
        return -1; // Chữ ký không hợp lệ
    }

    public String hmacSHA512(String key, String data) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA512");
            mac.init(new javax.crypto.spec.SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] hmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hmac) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create HMAC-SHA512", e);
        }
    }

    public boolean verifySignature(Map<String, String> fields, String vnpSecureHash) {
        Map<String, String> sortedFields = new TreeMap<>(fields);
        StringBuilder signValue = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedFields.entrySet()) {
            if (!entry.getKey().equals("vnp_SecureHash")) {
                signValue.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                        .append("&");
            }
        }
        if (signValue.length() > 0) {
            signValue.deleteCharAt(signValue.length() - 1);
        }
        String calculatedHash = hmacSHA512(vnPayConfig.vnp_HashSecret, signValue.toString());
        return calculatedHash.equals(vnpSecureHash);
    }
}