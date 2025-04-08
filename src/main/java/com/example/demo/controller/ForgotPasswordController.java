package com.example.demo.controller;

import com.example.demo.service.KhachHangService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("")
public class ForgotPasswordController {
    private static final Logger logger = LoggerFactory.getLogger(ForgotPasswordController.class);

    @Autowired
    private KhachHangService khachHangService;

    // Hiển thị form quên mật khẩu
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "dang_nhap/forgot_password";
    }

    // Xử lý quên mật khẩu
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, Model model) {
        try {
            khachHangService.quenMatKhau(email);
            model.addAttribute("success", "Một liên kết đặt lại mật khẩu đã được gửi đến email của bạn!");
            logger.info("Yêu cầu quên mật khẩu thành công cho email: {}", email);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            logger.error("Lỗi khi xử lý quên mật khẩu: {}", e.getMessage());
        }
        return "dang_nhap/forgot_password";
    }

    // Hiển thị form đặt lại mật khẩu
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "dang_nhap/reset_password";
    }

    // Xử lý đặt lại mật khẩu
    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("password") String password,
                                       Model model) {
        String result = khachHangService.datLaiMatKhau(token, password);
        if (result.contains("thành công")) {
            model.addAttribute("success", result);
            logger.info("Đặt lại mật khẩu thành công với token: {}", token);
        } else {
            model.addAttribute("error", result);
            logger.warn("Lỗi đặt lại mật khẩu: {}", result);
        }
        return "dang_nhap/reset_password";
    }
}