package com.example.demo.controller;

import com.example.demo.entity.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
            return userDetails.getHoTen();
        }
        return "Unknown User";
    }

    @GetMapping("/ttdn")
    public ResponseEntity<Map<String, Object>> getUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> response = new HashMap<>();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            if (auth.getPrincipal() instanceof CustomUserDetails userDetails) {
                response.put("authenticated", true);
                response.put("username", userDetails.getUsername());
                response.put("hoTen", userDetails.getHoTen());
            } else {
                response.put("authenticated", false); // Trường hợp bất ngờ
            }
        } else {
            response.put("authenticated", false); // Chưa đăng nhập
        }

        return ResponseEntity.ok(response); // Luôn trả về 200 OK
    }
}