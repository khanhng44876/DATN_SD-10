package com.example.demo.controller;

import com.example.demo.entity.NhanVien;
import com.example.demo.repository.NhanVienRepository;
import com.example.demo.repository.SanPhamRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/dang-nhap")
public class DangNhapController {

    @Autowired
    NhanVienRepository repo;
    @Autowired
    SanPhamRepository sanPhamRepository;

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("Nhân Viên", new NhanVien());
        return "dang_nhap/login";
    }

    @PostMapping("/LoginServlet")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        HttpSession session, Model model) {
        Optional<NhanVien> optionalTaiKhoan = repo.findByTaiKhoan(username);

        if (optionalTaiKhoan.isPresent()) {
            NhanVien taiKhoan = optionalTaiKhoan.get();
            if (taiKhoan.getMatKhau().equals(password)) {
                session.setAttribute("nhanVien", taiKhoan);
                System.out.println("Đăng nhập thành công: " + taiKhoan.getTaiKhoan()); // Debug

                return "redirect:/dang-nhap/index";
            }
        }

        model.addAttribute("error", "Tài khoản hoặc mật khẩu không đúng.");
        return "dang_nhap/login";
    }



    @GetMapping("/index")
    public String home(HttpSession session, Model model) {
        NhanVien nhanVien = (NhanVien) session.getAttribute("nhanVien");

        if (nhanVien == null) {
            System.out.println("Session không có nhân viên, chuyển hướng về login"); // Debug
            return "redirect:/dang-nhap/login";
        }

        model.addAttribute("nhanVien", nhanVien);
        return "dang_nhap/index";
    }


    @GetMapping("/hien-thi")
    public String hienThi(HttpSession session, Model model) {
        // Lấy thông tin người dùng từ session
        NhanVien taiKhoan = (NhanVien) session.getAttribute("nhanVien");

        // Nếu chưa đăng nhập, chuyển hướng về trang login
        if (taiKhoan == null) {
            return "redirect:/dang-nhap/login";
        }

        // Nếu không phải quản lý, hiển thị thông báo thay vì redirect để tránh vòng lặp
        if (taiKhoan.getChucVu() == null || !taiKhoan.getChucVu().getTenChucVu().equalsIgnoreCase("Quản Lý")) {
            model.addAttribute("error", "Bạn không có quyền truy cập trang này.");
            return "error/forbidden";  // Hiển thị trang lỗi thay vì redirect
        }

        // Nếu là Quản Lý, trả về danh sách nhân viên
        List<NhanVien> ls = repo.findAll();
        model.addAttribute("listtk", ls);
        return "nhan_vien/index";
    }







    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/dang-nhap/login";
    }


}