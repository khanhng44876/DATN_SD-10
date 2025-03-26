package com.example.demo.controller;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping
public class DangNhapController {

    private String getRedirectUrl(Authentication authentication) {
        if (authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("QUAN_LY"))) {
            return "/nhan-vien/hien-thi";
        } else if (authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("NHAN_VIEN"))) {
            return "/ban-hang-off/hien-thi";
        } else {
            return "/ban-hang-online"; // Mặc định cho khách hàng
        }
    }
    @GetMapping("/login")
    public String showLoginForm(Authentication authentication) {
        // Nếu user đã đăng nhập, chuyển hướng đến trang phù hợp
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:" + getRedirectUrl(authentication); // Hoặc điều hướng đúng trang theo quyền
        }
        return "dang_nhap/Login_Form";
    }
    @GetMapping("/index")
    public String home(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("nhanVien", auth.getName());
        return "dang_nhap/index";
    }
        @GetMapping("/logout")
        public String logout() {
            return "redirect:/login";
        }



    }