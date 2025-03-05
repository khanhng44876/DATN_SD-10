package com.example.demo.dto;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class SanPhamDto {
    @Column(name = "ma_san_pham")
    private String maSanPham;

    @Column(name = "ten_san_pham")
    private String tenSanPham;

    @DateTimeFormat(pattern = "yyyy/MM/dd")
    @Column(name = "ngay_nhap")
    private LocalDate ngayNhap;

    @DateTimeFormat(pattern = "yyyy/MM/dd")
    @Column(name = "ngay_sua")
    private LocalDate ngaySua;
    @Column(name = "trang_thai")
    private String trangThai;

    private Integer iddanhMuc;

    private Integer idhang;

}
