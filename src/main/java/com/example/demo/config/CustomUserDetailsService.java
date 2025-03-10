package com.example.demo.config;

import com.example.demo.entity.KhachHang;
import com.example.demo.entity.NhanVien;
import com.example.demo.repository.KhachHangRepository;
import com.example.demo.repository.NhanVienRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
@Transactional
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<NhanVien> nhanVien = nhanVienRepository.findByTaiKhoan(username);
        if (nhanVien.isPresent()) {
            NhanVien nv = nhanVien.get();
            // Truy cập ChucVu để đảm bảo nó được load
            if (nv.getChucVu() != null) {
                nv.getChucVu().getTenChucVu();
            }
            return new CustomUserDetails(nv);
        }

        Optional<KhachHang> khachHang = khachHangRepository.findByTaiKhoan(username);
        if (khachHang.isPresent()) {
            return new CustomUserDetails(khachHang.get());
        }

        throw new UsernameNotFoundException("Không tìm thấy user: " + username);
    }
}