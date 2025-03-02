package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class SanPhamDto {
    private String ma_san_pham;

    private String ten_san_pham;

    @DateTimeFormat(pattern = "yyyy/MM/dd")
    private LocalDate ngay_nhap;
    @DateTimeFormat(pattern = "yyyy/MM/dd")
    private LocalDate ngay_sua;

    private String trang_thai;

    private Integer iddanhMuc;

    private Integer idhang;

}
