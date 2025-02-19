package com.example.demo.controller;

import com.example.demo.repository.HangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/DATN_SD10")
public class HangController {
    @Autowired
    HangRepository hangRepository;
    // Hien thi
    @GetMapping("/hien-thi-hang")
    public String hienThi(Model model){
        model.addAttribute("listHang",hangRepository.findAll());
        return "hien-thi-hang";
    }

}
