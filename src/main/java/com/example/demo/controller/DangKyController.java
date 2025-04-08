package com.example.demo.controller;
import com.example.demo.entity.KhachHang;
import com.example.demo.service.KhachHangService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping()
public class DangKyController {
    @Autowired
    private KhachHangService khachHangService;

    // Hiển thị form đăng ký
    @GetMapping("/dang-ky")
    public String showDangKyForm(Model model) {
        model.addAttribute("khachHang", new KhachHang());
        return "dang_nhap/dang-ky"; // Trả về file dang-ky.html
    }

    // Xử lý đăng ký
    @PostMapping("/dang-ky")
    public String dangKyKhachHang(@Valid @ModelAttribute("khachHang") KhachHang khachHang,
                                  BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "dang_nhap/dang-ky"; // Nếu có lỗi validation, quay lại form
        }

        try {
            khachHangService.dangKyKhachHang(khachHang);
            model.addAttribute("message", "Đăng ký thành công! Vui lòng kiểm tra email để kích hoạt.");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "dang_nhap/dang-ky"; // Trả về trang đăng ký với thông báo
    }

    // Xử lý kích hoạt tài khoản
    @GetMapping("/kich-hoat")
    public String kichHoatTaiKhoan(@RequestParam("token") String token, Model model) {
        String message = khachHangService.kichHoatTaiKhoan(token);

        if (message.equals("Kích hoạt tài khoản thành công!")) {
            model.addAttribute("message", message);
        } else {
            model.addAttribute("error", message);
        }

        return "dang_nhap/kich-hoat"; // Trả về file kich-hoat.html
    }

}
