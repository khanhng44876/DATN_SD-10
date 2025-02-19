package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "san_pham")
public class SanPham {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    String ma_san_pham;

    String ten_san_pham;

    Date ngay_nhap;

    Date ngay_sua;

    String trang_thai;

    @ManyToOne
    @JoinColumn(name = "id_san_pham")
    SanPhamChiTiet san_pham_chi_tiet;
}
