package com.example.demo.controller;

import com.example.demo.dto.HoaDonRequest;
import com.example.demo.dto.SpctDto;
import com.example.demo.entity.*;
import com.example.demo.exception.MessageException;
import com.example.demo.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Controller

public class BanHangOnlineController {
    @Autowired
    private SanPhamRepository spRepository;
    @Autowired
    private SanPhamCTRepository ctsp_repository;
    @Autowired
    private KhachHangRepository khachHangRepository;
    @Autowired
    private HoaDonRepossitory hoaDonrepo;
    @Autowired
    private HoaDonCTRepository hdctRepository;
    @Autowired
    private DanhMucRepository danhMucRepository;


    @RequestMapping("/ban-hang-online")
    public String iddd(Model model) {
        List<SanPhamChiTiet> spClb = ctsp_repository.findAll().stream()
                .filter(sp->sp.getSanPham().getDanhMuc().getId() == 1)
                .filter(sp->sp.getSanPham().getTrangThai().equals("Còn hàng"))
                .toList();
        List<SanPhamChiTiet> spDtqg = ctsp_repository.findAll().stream()
                .filter(sp->sp.getSanPham().getDanhMuc().getId() == 2)
                .filter(sp->sp.getSanPham().getTrangThai().equals("Còn hàng"))
                .limit(5)
                .toList();
        List<SanPhamChiTiet> spNologo = ctsp_repository.findAll().stream()
                .filter(sp->sp.getSanPham().getDanhMuc().getId() == 3)
                .filter(sp->sp.getSanPham().getTrangThai().equals("Còn hàng"))
                .limit(5)
                .toList();
        model.addAttribute("spDtqg",spDtqg);
        model.addAttribute("spNologo",spNologo);
        model.addAttribute("spClb", spClb);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Kiểm tra nếu user chưa đăng nhập (anonymous)
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            return "ban_hang_online/home";  // Trang chưa đăng nhập
        }

        return "ban_hang_online/index";  // Trang đã đăng nhập
    }

    @RequestMapping("/ban-hang-online/cart")
    public String idd(Model model){
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        KhachHang khachHang = khachHangRepository.findById(userDetails.getId()).orElse(null);
        model.addAttribute("kh",khachHang);
        return "ban_hang_online/giohang.html";
    }
    @RequestMapping("/ban-hang-online/sp")
    public String iddsp(Model model){
        model.addAttribute("listSp", ctsp_repository.findAll());
        return "ban_hang_online/product.html";
    }




    // Thêm phương thức mới để lấy chi tiết sản phẩm theo ID
    @GetMapping("/ban-hang-online/ctsp/{id}")
    @ResponseBody
    public ResponseEntity<?> getProductDetail(@PathVariable("id") Integer id) {
        try {
            Optional<SanPhamChiTiet> optionalCtsp = ctsp_repository.findById(id);
            if (!optionalCtsp.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy sản phẩm với ID: " + id);
            }
            SanPhamChiTiet ctsp = optionalCtsp.get();
            return new ResponseEntity<>(ctsp, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi lấy chi tiết sản phẩm: " + e.getMessage());
        }
    }
    @GetMapping("/ban-hang-online/detail")
    public String detailPage(@RequestParam("id") Integer id, Model model) {
        Optional<SanPhamChiTiet> optionalCtsp = ctsp_repository.findById(id);
        if (!optionalCtsp.isPresent()) {
            return "ban_hang_online/error";
        }
        model.addAttribute("product", optionalCtsp.get());
        return "ban_hang_online/detail"; // Hiển thị trang Thymeleaf
    }


}
