package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Date;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "khach_hang")
public class KhachHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Tên khách hàng không được để trống")
    @Column(name = "ten_khach_hang", length = 100, nullable = false)
    private String tenKhachHang;

    @Email(message = "Email không hợp lệ")
    @Column(name = "email", unique = true)
    private String email;

    @Pattern(regexp = "^(0|\\+84)[0-9]{9}$",
            message = "Số điện thoại không hợp lệ")
    @Column(name = "so_dien_thoai", length = 15)
    private String soDienThoai;

    @Column(name = "dia_chi", length = 255)
    private String diaChi;

    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "ngay_sinh")
    private Date ngaySinh;

    @Column(name = "tai_khoan", unique = true, length = 100)
    private String taiKhoan;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Column(name = "mat_khau")
    private String matKhau;

    @Column(name = "gioi_tinh", length = 10)
    private String gioiTinh;

    public boolean isAdult() {
        if (ngaySinh == null) return false;
        Date now = new Date(System.currentTimeMillis());
        long diffInYears = (now.getTime() - ngaySinh.getTime()) / (1000L * 60 * 60 * 24 * 365);
        return diffInYears >= 18;
    }

    @PrePersist
    @PreUpdate
    private void validateBeforeSave() {
        if (tenKhachHang != null) {
            tenKhachHang = tenKhachHang.trim();
        }
        if (email != null) {
            email = email.toLowerCase().trim();
        }
    }
}