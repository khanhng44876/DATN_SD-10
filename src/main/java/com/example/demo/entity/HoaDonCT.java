package com.example.demo.entity;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "hoa_don_chi_tiet")
public class HoaDonCT {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Integer id;
    private Integer id_hoa_don;
    private Integer id_san_pham_chi_tiet;
    private Integer so_luong;
    private float don_gia;
    private float tong_tien;
    private float thanh_tien;
    private String trang_thai;
    @ManyToOne @JoinColumn(name ="id_san_pham_chi_tiet", insertable = false, updatable = false )
    private SanPhamChiTiet sanPhamChiTiet;
    @ManyToOne @JoinColumn(name = "id_hoa_don", insertable = false, updatable = false)
    private HoaDonCT hoaDonCT;
}
