package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "nhan_vien")
public class NhanVien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @Column(name = "tai_khoan", unique = true, length = 100)
    private String taiKhoan;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Column(name = "mat_khau")
    private String matKhau;

    @Column(name = "ten_nhan_vien", length = 100)
    private String tenNhanVien;

    @Email(message = "Email không hợp lệ")
    @Column(name = "email", unique = true)
    private String email;

    @Pattern(regexp = "^(0|\\+84)[0-9]{9}$", message = "Số điện thoại không hợp lệ")
    @Column(name = "sdt", length = 10)
    private String sdt;

    @Column(name = "dia_chi", length = 200)
    private String diaChi;

    @Column(name = "gioi_tinh", length = 10)
    private String gioiTinh;

    @Column(name = "chuc_vu")
    private String chucVu;

    @CreationTimestamp
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "ngay_tao", updatable = false)
    private Date ngayTao;

    @UpdateTimestamp
    @Column(name = "ngay_sua")
    private Date ngaySua;

    @Column(name = "trang_thai", length = 50)
    private String trangThai;

    @PrePersist
    protected void onCreate() {
        ngayTao = new Date();
        ngaySua = new Date();
        if (trangThai == null) {
            trangThai = "Đang hoạt động";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        ngaySua = new Date();
    }
}