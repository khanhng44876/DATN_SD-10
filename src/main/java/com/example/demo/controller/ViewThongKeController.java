package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewThongKeController {

    @GetMapping("/thong-ke")
    public String thongKeView() {
        return "thong_ke/bieudo";
    }
}
