package com.example.demo.controller;

import com.example.demo.entity.NhanVien;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;



@Controller
public class DangNhapController {

    @GetMapping("/login")
    public String showLoginForm(Authentication authentication) {
        // Nếu đã đăng nhập rồi thì chuyển hướng luôn
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "/dang_nhap/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }

        // Lấy role đầu tiên của user
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("");

        // Chuyển hướng theo role
        return switch (role) {
            case "ROLE_ADMIN" -> "redirect:hoa-don";
            case "ROLE_NHAN_VIEN" -> "redirect:/san_pham/qlsp";
            case "ROLE_KHACH_HANG" -> "redirect:/home";
            default -> "redirect:/login";
        };
    }

    // Thêm xử lý lỗi
    @ExceptionHandler(Exception.class)
    public String handleError(Exception e) {
        System.out.println("Lỗi: " + e.getMessage());
        return "redirect:/login?error=true";
    }

    @GetMapping("/index")
    public String home(HttpSession session, Model model) {
        NhanVien nhanVien = (NhanVien) session.getAttribute("nhanVien");
        if (nhanVien == null) {
            return "redirect:/dang-nhap/login"; // Chuyển hướng về trang đăng nhập nếu chưa đăng nhập
        }
        model.addAttribute("nhanVien", nhanVien); // Truyền đối tượng TaiKhoan vào model
        return "dang_nhap/index"; // Trả về trang index.html nếu đã đăng nhập
    }

    @GetMapping("/hien-thi")
    public String hienThi(HttpSession session, Model model) {
        // Lấy thông tin người dùng từ session
        NhanVien nhanVien = (NhanVien) session.getAttribute("nhanVien");
        if (nhanVien == null || !nhanVien.getChucVu().getTenChucVu().equalsIgnoreCase("QL")) {
            return "redirect:/dang-nhap/login"; // Chuyển hướng nếu không phải Quản Lý
        }
        return "hoa-don/doanhThu";
    }
    @GetMapping("/logout")
    public String logout (HttpSession session){
        session.invalidate();
        return "redirect:/dang-nhap/login";
    }
}