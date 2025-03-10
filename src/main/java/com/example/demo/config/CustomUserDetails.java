package com.example.demo.config;

import com.example.demo.entity.KhachHang;
import com.example.demo.entity.NhanVien;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


import java.util.Collection;
import java.util.Collections;

// CustomUserDetails.java
public class CustomUserDetails implements UserDetails {
    private Integer id;
    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(NhanVien nhanVien) {
        this.id = nhanVien.getId();
        this.username = nhanVien.getTaiKhoan();
        this.password = nhanVien.getMatKhau();

        // Xác định role dựa trên tên chức vụ
        if (nhanVien.getChucVu() != null) {
            String roleName = switch (nhanVien.getChucVu().getTenChucVu().toUpperCase()) {
                case "QUẢN LÝ" -> "ROLE_ADMIN";
                case "NHÂN VIÊN" -> "ROLE_NHAN_VIEN";
                default -> "ROLE_USER";
            };
            this.authorities = Collections.singletonList(new SimpleGrantedAuthority(roleName));
        }
    }

    // Constructor cho KhachHang
    public CustomUserDetails(KhachHang khachHang) {
        this.id = khachHang.getId();
        this.username = khachHang.getTaiKhoan();
        this.password = khachHang.getMatKhau();
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_KHACH_HANG"));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
