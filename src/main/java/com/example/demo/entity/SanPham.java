package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

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
    @Column(name = "ma_san_pham")
    private String maSanPham;

    @Column (name = "ten_san_pham")
    private String tenSanPham;

    @DateTimeFormat(pattern = "yyyy/MM/dd")
    @Column(name = "ngay_nhap")
    private LocalDate ngayNhap;

    @DateTimeFormat(pattern = "yyyy/MM/dd")
    @Column(name = "ngay_sua")
    private LocalDate ngaySua;

    @Column(name = "trang_thai")
    private String trangThai;


    @ManyToOne
    @JoinColumn(name = "id_danh_muc")
    private DanhMuc danhMuc;

    @ManyToOne
    @JoinColumn(name = "id_hang")
    private Hang hang;
}
