package com.example.demo.dto;

import java.util.List;
import java.util.Map;

public class ThongKeDTO {
    private Map<String, Object> doanhSoThang;
    private Map<String, Object> doanhSoHomNay;
    private Integer tongSanPhamDaBan;
    private List<Map<String, Object>> sanPhamBanChay;
    private List<Map<String, Object>> sanPhamSapHet;
    private List<Map<String, Object>> bieuDoHangNgay;
    private Map<String, Object> trangThaiDonHang;

    public Map<String, Object> getDoanhSoThang() {
        return doanhSoThang;
    }

    public void setDoanhSoThang(Map<String, Object> doanhSoThang) {
        this.doanhSoThang = doanhSoThang;
    }

    public Map<String, Object> getDoanhSoHomNay() {
        return doanhSoHomNay;
    }

    public void setDoanhSoHomNay(Map<String, Object> doanhSoHomNay) {
        this.doanhSoHomNay = doanhSoHomNay;
    }

    public Integer getTongSanPhamDaBan() {
        return tongSanPhamDaBan;
    }

    public void setTongSanPhamDaBan(Integer tongSanPhamDaBan) {
        this.tongSanPhamDaBan = tongSanPhamDaBan;
    }

    public List<Map<String, Object>> getSanPhamBanChay() {
        return sanPhamBanChay;
    }

    public void setSanPhamBanChay(List<Map<String, Object>> sanPhamBanChay) {
        this.sanPhamBanChay = sanPhamBanChay;
    }

    public List<Map<String, Object>> getSanPhamSapHet() {
        return sanPhamSapHet;
    }

    public void setSanPhamSapHet(List<Map<String, Object>> sanPhamSapHet) {
        this.sanPhamSapHet = sanPhamSapHet;
    }

    public List<Map<String, Object>> getBieuDoHangNgay() {
        return bieuDoHangNgay;
    }

    public void setBieuDoHangNgay(List<Map<String, Object>> bieuDoHangNgay) {
        this.bieuDoHangNgay = bieuDoHangNgay;
    }

    public Map<String, Object> getTrangThaiDonHang() {
        return trangThaiDonHang;
    }

    public void setTrangThaiDonHang(Map<String, Object> trangThaiDonHang) {
        this.trangThaiDonHang = trangThaiDonHang;
    }
}

