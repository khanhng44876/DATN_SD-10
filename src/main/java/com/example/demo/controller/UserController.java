package com.example.demo.controller;

import com.example.demo.entity.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {
    @GetMapping("/user-info")
    public String getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getHoTen(); // Trả về họ tên
        }

        return "Unknown User";
    }
    @GetMapping("/")
    public String home(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("userName", userDetails.getUsername());
        } else {
            model.addAttribute("isLoggedIn", false);
        }

        return "ban_hang_online/index"; // Trả về trang giao diện chính
    }
    @GetMapping("/ttdn")
    public ResponseEntity<Map<String, Object>> getUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> response = new HashMap<>();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            // Nếu chưa đăng nhập -> Trả về lỗi 401 (Unauthorized)
            response.put("error", "Chưa đăng nhập");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        if (auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            // Nếu đã đăng nhập -> Trả về thông tin user
            response.put("username", userDetails.getUsername());
            response.put("hoTen", userDetails.getHoTen());
            response.put("authenticated", true);
            return ResponseEntity.ok(response);
        }

        // Trường hợp khác (có lỗi gì đó)
        response.put("error", "Không xác định");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

