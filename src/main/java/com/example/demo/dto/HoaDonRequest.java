package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class HoaDonRequest {

    private LocalDate ngayTao;
    private float tongTien;
    private String hinhThucThanhToan;
    private String trangThaiThanhToan;
    private Long khId; // ID của khách hàng
    private String tenKhachHang;
    private String sdt;
    private String diaChi;
    private Integer taiKhoanId;
    List<SpctDto> spct = new ArrayList<>();

    private String hoTen;

    private String soDienThoai;

    private String hinhThucThanhToanl;

    private Integer discountId;
}
