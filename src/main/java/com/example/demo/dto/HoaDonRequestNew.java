package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Getter
@Setter
public class HoaDonRequestNew {
    @JsonProperty("ngay_tao")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date ngayTao;

    @JsonProperty("ngay_sua")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date ngaySua;

    @JsonProperty("don_gia")
    private Float donGia;

    @JsonProperty("tong_tien")
    private Float tongTien;

    @JsonProperty("trang_thai_thanh_toan")
    private String trangThaiThanhToan;

    @JsonProperty("hinh_thuc_thanh_toan")
    private String hinhThucThanhToan;

    @JsonProperty("dia_chi_giao_hang")
    private String diaChiGiaoHang;

    @JsonProperty("ghi_chu")
    private String ghiChu;

    @JsonProperty("id_khach_hang")
    private Integer idKhachHang;

    @JsonProperty("id_nhan_vien")
    private Integer idNhanVien;

    @JsonProperty("id_khuyen_mai")
    private Integer idKhuyenMai;
}