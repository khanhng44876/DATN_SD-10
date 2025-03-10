package com.example.demo.controller;

import com.example.demo.dto.ChiTietSanPhamDto;
import com.example.demo.entity.SanPhamChiTiet;
import com.example.demo.repository.SanPhamCTRepository;
import com.example.demo.repository.SanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ban-hang-off")
public class BanHangController {
    @Autowired
    SanPhamCTRepository ctRepository;

    @Autowired
    SanPhamRepository spRepository;

    @GetMapping("/hien-thi")
    public String hienThi(Model model) {
        model.addAttribute("listCTSP", ctRepository.findAll());
        return "/sell/index.html";
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<SanPhamChiTiet> detail(@PathVariable Integer id) {
        return ResponseEntity.ok(ctRepository.findById(id).get());
    }
}
