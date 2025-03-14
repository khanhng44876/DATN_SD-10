package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class DonHangDto {

    List<SpctDto> spct = new ArrayList<>();

    private String hoTen;

    private String soDienThoai;

    private String hinhThucThanhToan;

    private Integer discountId;
}
