package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "khuyen_mai")
public class KhuyenMai {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Integer id;

    Integer so_luong;

    String ten_khuyen_mai;

    String mo_ta;

    String ma_khuyen_mai;

    Integer muc_giam;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate ngay_bat_dau;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate ngay_ket_thuc;

    Double dieu_kien;

    Double giam_toi_da;

    String trang_thai;
}
