package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChiTietSanPhamDto {

    private Integer idsanPham;
    private Integer idmauSac;
    private Integer idkichThuoc;
    private Integer idchatLieu;
    private Integer soLuong;
    private Float donGia;
    private String moTa;
    private String anhSanPham;
}
