package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Date;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table("hoa_don")
public class HoaDon {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @DateTimeFormat(pattern = "yyyy/MM/dd")
    @Temporal(TemporalType.DATE)
    private Date ngay_tao;
    private Date ngay_sua;
    private Float don_gia;
    private Float tong_tien;
    private String trang_thai_thanh_toan;
    private String hinh_thuc_thanh_toan;
    private String dia_chi_giao_hang;
    private String ghi_chu;
//    @ManyToOne @JoinColumn(name = "id_khach_hang")
//    private KhachHang khachHang;
//    @ManyToOne @JoinColumn("nhan_vien")
//    private NhanVien nhanVien;



}
