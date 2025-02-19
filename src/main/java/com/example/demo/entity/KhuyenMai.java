package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "khuyen_mai")
public class KhuyenMai {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    String ten_khuyen_mai;

    String mo_ta;

    Double ma_giam;

    Double muc_giam;

    Date ngay_bat_dau;

    Date ngay_ket_thuc;

    String trang_thai;

    Double dieu_kien;
}
