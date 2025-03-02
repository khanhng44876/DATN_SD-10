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
@Table(name = "san_pham")
public class SanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String ma_san_pham;

    private String ten_san_pham;
    @DateTimeFormat(pattern = "yyyy/MM/dd")
    private LocalDate ngay_nhap;
    @DateTimeFormat(pattern = "yyyy/MM/dd")
    private LocalDate ngay_sua;

    private String trang_thai;


    @ManyToOne
    @JoinColumn(name = "id_danh_muc")
    private DanhMuc danhMuc;

    @ManyToOne
    @JoinColumn(name = "id_hang")
    private Hang hang;
}
