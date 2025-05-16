package com.example.demo.controller;

import com.example.demo.entity.CustomUserDetails;
import com.example.demo.entity.ThongBao;
import com.example.demo.repository.ThongBaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ControllerAdvice
public class ThongBaoController {
    @Autowired
    ThongBaoRepository repo;

    @ModelAttribute("noti")
    public List<ThongBao> getNoti(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if(userDetails == null){
            return Collections.emptyList(); //Chưa đăng nhập thì trả về list rỗng
        }
        return repo.findByKhachHang_IdOrderByNgayTaoDesc(userDetails.getId());//Trả về list noti của người dùng đó
    }

    @ModelAttribute("notiCount")
    public Long getNotiCount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if(userDetails == null){
            return 0L;
        }
        return repo.countByKhachHang_IdAndReadFalse(userDetails.getId());
    }

}
