package com.example.demo.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "danh_muc")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DanhMuc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Column(name = "ten_danh_muc")
    private String tendanhmuc;
    @Column(name = "mo_ta")
    private String mota;
    @Column(name = "trang_thai")
    private String trangThai;

}
