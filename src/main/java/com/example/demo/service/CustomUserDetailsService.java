package com.example.demo.service;

import com.example.demo.entity.CustomUserDetails;
import com.example.demo.entity.KhachHang;
import com.example.demo.entity.NhanVien;
import com.example.demo.repository.KhachHangRepository;
import com.example.demo.repository.NhanVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Đang kiểm tra tài khoản: " + username);
        Optional<NhanVien> nhanVien = nhanVienRepository.findByTaiKhoan(username);
        if (nhanVien.isPresent()) {
            return new CustomUserDetails(nhanVien.get());
        }

        Optional<KhachHang> khachHang = khachHangRepository.findByTaiKhoan(username);
        if (khachHang.isPresent()) {
            return new CustomUserDetails(khachHang.get());
        }

        throw new UsernameNotFoundException("Không tìm thấy tài khoản");
    }
}
