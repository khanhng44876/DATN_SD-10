package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Getter
@Setter
public class HoaDonCT_DTO {
    @JsonProperty("id_hoa_don")
    private Integer idHoaDon;

    @JsonProperty("id_san_pham_chi_tiet")
    private Integer idSanPhamChiTiet;

    @JsonProperty("so_luong")
    private Integer soLuong;

    @JsonProperty("don_gia")
    private Float donGia;

    @JsonProperty("tong_tien")
    private Float tongTien;

    @JsonProperty("thanh_tien")
    private Float thanhTien;

    @JsonProperty("ngay_tao")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date ngayTao;

    @JsonProperty("ngay_sua")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date ngaySua;

    @JsonProperty("trang_thai")
    private String trangThai;

}
