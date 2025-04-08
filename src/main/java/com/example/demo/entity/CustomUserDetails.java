package com.example.demo.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails{
    private final Integer id;
    private final String username;
    private final String password;
    private final String hoTen;
    private final String email;
    private final Boolean trangThai;
    private final List<GrantedAuthority> authorities;

    public CustomUserDetails(NhanVien nhanVien) {
        this.id = nhanVien.getId();
        this.hoTen = nhanVien.getTenNhanVien();
        this.username = nhanVien.getTaiKhoan();
        this.password = nhanVien.getMatKhau();
        this.email = nhanVien.getEmail();
        this.trangThai = nhanVien.getTrangThai();
        this.authorities = List.of(new SimpleGrantedAuthority(nhanVien.getChucVu())); // QUAN_LY, NHAN_VIEN
    }

    public CustomUserDetails(KhachHang khachHang) {
        this.id = khachHang.getId();
        this.hoTen = khachHang.getTenKhachHang();
        this.username = khachHang.getTaiKhoan();
        this.password = khachHang.getMatKhau();
        this.email = khachHang.getEmail();
        this.trangThai = khachHang.getTrangThai();
        this.authorities = List.of(new SimpleGrantedAuthority("KHACH_HANG"));
    }

    public Integer getId() {return id;}

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
    public boolean isEnabled() {
        return trangThai; // Chỉ cho phép đăng nhập nếu tài khoản đã kích hoạt
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

}
