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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;


@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    public CustomUserDetailsService(NhanVienRepository nhanVienRepository, KhachHangRepository khachHangRepository) {
        this.nhanVienRepository = nhanVienRepository;
        this.khachHangRepository = khachHangRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {
        if (input == null || input.isEmpty()) {
            throw new UsernameNotFoundException("Tên đăng nhập hoặc email không được để trống");
        }

        logger.info("Đang kiểm tra tài khoản với input: {}", input);

        // Kiểm tra NhanVien bằng taiKhoan hoặc email
        Optional<NhanVien> nhanVienByTaiKhoan = nhanVienRepository.findByTaiKhoan(input);
        if (nhanVienByTaiKhoan.isPresent()) {
            NhanVien nhanVien = nhanVienByTaiKhoan.get();
            if (nhanVien.getTrangThai() == null || !nhanVien.getTrangThai()) {
                logger.warn("Tài khoản nhân viên {} bị vô hiệu hóa", input);
                throw new UsernameNotFoundException("Tài khoản đã bị vô hiệu hóa: " + input);
            }
            logger.debug("Tìm thấy nhân viên bằng tài khoản: {}", input);
            return new CustomUserDetails(nhanVien);
        }

        Optional<NhanVien> nhanVienByEmail = nhanVienRepository.findByEmail(input);
        if (nhanVienByEmail.isPresent()) {
            NhanVien nhanVien = nhanVienByEmail.get();
            if (nhanVien.getTrangThai() == null || !nhanVien.getTrangThai()) {
                logger.warn("Tài khoản nhân viên {} bị vô hiệu hóa", input);
                throw new UsernameNotFoundException("Tài khoản đã bị vô hiệu hóa: " + input);
            }
            logger.debug("Tìm thấy nhân viên bằng email: {}", input);
            return new CustomUserDetails(nhanVien);
        }

        // Kiểm tra KhachHang bằng taiKhoan hoặc email
        Optional<KhachHang> khachHangByTaiKhoan = khachHangRepository.findByTaiKhoan(input);
        if (khachHangByTaiKhoan.isPresent()) {
            KhachHang khachHang = khachHangByTaiKhoan.get();
            if (khachHang.getTrangThai() == null || !khachHang.getTrangThai()) {
                logger.warn("Tài khoản khách hàng {} bị vô hiệu hóa", input);
                throw new UsernameNotFoundException("Tài khoản đã bị vô hiệu hóa: " + input);
            }
            logger.debug("Tìm thấy khách hàng bằng tài khoản: {}", input);
            return new CustomUserDetails(khachHang);
        }

        Optional<KhachHang> khachHangByEmail = khachHangRepository.findByEmail(input);
        if (khachHangByEmail.isPresent()) {
            KhachHang khachHang = khachHangByEmail.get();
            if (khachHang.getTrangThai() == null || !khachHang.getTrangThai()) {
                logger.warn("Tài khoản khách hàng {} bị vô hiệu hóa", input);
                throw new UsernameNotFoundException("Tài khoản đã bị vô hiệu hóa: " + input);
            }
            logger.debug("Tìm thấy khách hàng bằng email: {}", input);
            return new CustomUserDetails(khachHang);
        }

        logger.warn("Không tìm thấy tài khoản hoặc email: {}", input);
        throw new UsernameNotFoundException("Không tìm thấy tài khoản hoặc email: " + input);
    }
}
