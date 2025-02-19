package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "san_pham_chi_tiet")
public class SanPhamChiTiet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    Double don_gia;

    Integer so_luong;

    String mo_ta;

    String hinh_anh;

    String trang_thai;

    @ManyToOne
    @JoinColumn(name = "id_chat_lieu")
    ChatLieu chatLieu;

    @ManyToOne
    @JoinColumn(name = "id_kich_thuoc")
    KichThuoc kichThuoc;

    @ManyToOne
    @JoinColumn(name = "id_san_pham")
    SanPham sanPham;

    @ManyToOne
    @JoinColumn(name = "id_mau_sac")
    MauSac mauSac;
}
