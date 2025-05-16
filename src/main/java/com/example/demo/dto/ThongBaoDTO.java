package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ThongBaoDTO {
    private int id;
    private String noi_dung;
    private String link;
    private Date ngay_tao;
}
