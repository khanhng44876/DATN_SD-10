package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "chuc_vu")
public class ChucVu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
//    @Column
//    private String ten_chuc_vu;
    @Column(name = "ten_chuc_vu")
    private String tenChucVu;
    @Column(name = "trang_thai")
    private String trangThai;
}
//    id INT IDENTITY(1,1) PRIMARY KEY,
//    ten_chuc_vu NVARCHAR(50) NOT NULL,
//    trang_thai NVARCHAR(50) DEFAULT N'Đang hoạt động'