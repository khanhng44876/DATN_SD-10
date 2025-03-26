package com.example.demo.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails{
    private final String username;
    private final String password;
    private final String hoTen;
    private final List<GrantedAuthority> authorities;

    public CustomUserDetails(NhanVien nhanVien) {
        this.hoTen = nhanVien.getTenNhanVien();
        this.username = nhanVien.getTaiKhoan();
        this.password = nhanVien.getMatKhau();
        this.authorities = List.of(new SimpleGrantedAuthority(nhanVien.getChucVu())); // QUAN_LY, NHAN_VIEN
    }

    public CustomUserDetails(KhachHang khachHang) {
        this.hoTen = khachHang.getTenKhachHang();
        this.username = khachHang.getTaiKhoan();
        this.password = khachHang.getMatKhau();
        this.authorities = List.of(new SimpleGrantedAuthority("KHACH_HANG"));
    }
    public String getHoTen() {
        return hoTen;
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
