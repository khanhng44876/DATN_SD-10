package com.example.demo.controller;

import com.example.demo.service.VNPAYService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/vnpay")
public class VNPAYController {

    @Autowired
    private VNPAYService vnpayService;

    @PostMapping("/create-payment")
    public ResponseEntity<Map<String, String>> createPayment(
            @RequestParam("amount") int amount,
            @RequestParam("orderInfo") String orderInfo,
            HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        String paymentUrl = vnpayService.createOrder(request, amount, orderInfo, baseUrl);

        Map<String, String> response = new HashMap<>();
        response.put("paymentUrl", paymentUrl);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/return")
    public ResponseEntity<Map<String, Object>> paymentReturn(HttpServletRequest request) {
        int paymentStatus = vnpayService.orderReturn(request);

        Map<String, Object> response = new HashMap<>();
        response.put("status", paymentStatus == 1 ? "Success" : paymentStatus == 0 ? "Failed" : "Invalid Signature");
        response.put("orderInfo", request.getParameter("vnp_OrderInfo"));
        response.put("amount", Integer.parseInt(request.getParameter("vnp_Amount")) / 100);
        response.put("paymentTime", request.getParameter("vnp_PayDate"));
        response.put("transactionId", request.getParameter("vnp_TransactionNo"));

        return ResponseEntity.ok(response);
    }
}
