package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Entity
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "nhan_vien")
public class NhanVien {
    @ManyToOne
    @JoinColumn(name = "id_chuc_vu")
    private ChucVu chucVu;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    @Column
    private String tai_khoan;
    @Column
    private String mat_khau;
    @Column
    private String ten_nhan_vien;
    @Column
    private String email;
    @Column
    private String sdt;
    @Column
    private String dia_chi;
    @Column
    private String gioi_tinh;
    @DateTimeFormat(pattern = "yyyy/MM/dd")
    @Temporal(TemporalType.DATE)
    @Column
    private Date ngay_tao;
    @Column
    private Date ngay_sua;
    @Column
    private String trang_thai;
}
