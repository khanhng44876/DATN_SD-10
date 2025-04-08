package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class OrderOnlineDto {
    private HoaDonRequestNew hoaDon;
    private List<HoaDonCT_DTO> chiTiet;

    // Getters và Setters
    public HoaDonRequestNew getHoaDon() {
        return hoaDon;
    }

    public void setHoaDon(HoaDonRequestNew hoaDon) {
        this.hoaDon = hoaDon;
    }

    public List<HoaDonCT_DTO> getChiTiet() {
        return chiTiet;
    }

    public void setChiTiet(List<HoaDonCT_DTO> chiTiet) {
        this.chiTiet = chiTiet;
    }
}
