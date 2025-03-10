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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    @Column (name = "tai_khoan")
    private String tenDangNhap;
    @Column (name = "mat_khau")
    private String matKhau;
    @Column(name = "ten_nhan_vien")
    private String ten_nhan_vien;

    private String email;

    private String sdt;
    @Column(name = "dia_chi")
    private String dia_chi;
    @Column(name = "gioi_tinh")
    private String gioi_tinh;
    @DateTimeFormat(pattern = "yyyy/MM/dd")
    @Temporal(TemporalType.DATE)
    @Column(name = "ngay_tao")
    private Date ngayTao;
    @DateTimeFormat(pattern = "yyyy/MM/dd")
    @Temporal(TemporalType.DATE)
    @Column(name = "ngay_sua")
    private Date ngay_sua;
    @Column (name = "trang_thai")
    private String trangThai;

    @ManyToOne
    @JoinColumn(name = "id_chuc_vu")
    private ChucVu chucVu;
}