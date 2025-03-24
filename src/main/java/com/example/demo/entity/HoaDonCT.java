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
    @Column(name = "id_hoa_don")
    private int hoaDon_id;
    @Column(name = "id_san_pham_chi_tiet")
    private int ctsp_id;

    @Column(name = "so_luong")
    private Integer soLuong;

    @Column(name = "don_gia")
    private Float donGia;

    @Column(name = "tong_tien")
    private Float tongTien;

    @Column(name = "thanh_tien")
    private Float thanhTien;

    @Column(name = "trang_thai")
    private String trangThai;

    @ManyToOne @JoinColumn(name ="id_san_pham_chi_tiet", insertable = false, updatable = false )
    private SanPhamChiTiet sanPhamChiTiet;
    @ManyToOne @JoinColumn(name = "id_hoa_don", insertable = false, updatable = false)
    private HoaDon hoaDon;

    @Column(name = "ngay_tao")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date ngayTao;

    @Column(name = "ngay_sua")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date ngaySua;
}
