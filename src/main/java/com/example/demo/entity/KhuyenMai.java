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
   private Integer id;

   private String ten_khuyen_mai;

   private String mo_ta;

   private Double ma_giam;

   private Double muc_giam;

   private Date ngay_bat_dau;

    Date ngay_ket_thuc;

    String trang_thai;

    Double dieu_kien;
}
