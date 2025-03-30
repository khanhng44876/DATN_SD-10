package com.example.demo.controller;

import com.example.demo.dto.ThongKeDTO;
import com.example.demo.service.ThongKeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/thong-ke")
public class ThongKeController {

    @Autowired
    private ThongKeService thongKeService;

    @GetMapping("/tong-quat")
    public ThongKeDTO getThongKeTongQuat() {
        return thongKeService.getThongKeTongQuat();
    }

    @GetMapping("/monthly-sales")
    public Map<String, Object> getMonthlySales() {
        return thongKeService.getMonthlySales();
    }

    @GetMapping("/today-sales")
    public Map<String, Object> getTodaySales() {
        return thongKeService.getTodaySales();
    }

    @GetMapping("/monthly-sold-products")
    public Integer getMonthlySoldProducts() {
        return thongKeService.getMonthlySoldProductCount();
    }

    @GetMapping("/low-stock-products")
    public List<Map<String, Object>> getLowStockProducts(@RequestParam(defaultValue = "10") int threshold) {
        return thongKeService.getLowStockProducts(threshold);
    }

    @GetMapping("/best-selling-products")
    public List<Map<String, Object>> getBestSellingProducts(
            @RequestParam(name = "limit", defaultValue = "5") int limit) {
        return thongKeService.getBestSellingProducts(limit);
    }


    @GetMapping("/monthly-chart-data")
    public List<Map<String, Object>> getMonthlyChartData() {
        return thongKeService.getMonthlyChartData();
    }

    @GetMapping("/order-status")
    public Map<String, Object> getOrderStatusChart() {
        return thongKeService.getOrderStatusData();
    }
}