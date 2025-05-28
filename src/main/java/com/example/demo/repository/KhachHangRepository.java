package com.example.demo.repository;

import com.example.demo.entity.KhachHang;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {
//    Optional<KhachHang> findByTenKhachHang(@NotBlank(message = "Tên khách hàng không được để trống") String tenKhachHang);
    Optional<KhachHang> findByTaiKhoan(String taiKhoan);
//    KhachHang findBytaiKhoan(String taiKhoan);
Optional<KhachHang> findByEmail(String email);

    boolean existsByTenKhachHangAndIdNot(@NotBlank(message = "Tên khách hàng không được để trống") String tenKhachHang, Integer id);

    boolean existsByTenKhachHang(@NotBlank(message = "Tên khách hàng không được để trống") String tenKhachHang);

    boolean existsByEmail(@Email(message = "Email không hợp lệ") String email);

    boolean existsBySoDienThoai(@Pattern(regexp = "^(0|\\+84)[0-9]{9}$",
            message = "Số điện thoại không hợp lệ") String soDienThoai);

    boolean existsByNgaySinh(@Past(message = "Ngày sinh phải là ngày trong quá khứ") LocalDate ngaySinh);

    boolean existsByEmailAndIdNot(@Email(message = "Email không hợp lệ") String email, Integer id);

    boolean existsBySoDienThoaiAndIdNot(@Pattern(regexp = "^(0|\\+84)[0-9]{9}$",
            message = "Số điện thoại không hợp lệ") String soDienThoai, Integer id);

    boolean existsByNgaySinhAndIdNot(@Past(message = "Ngày sinh phải là ngày trong quá khứ") LocalDate ngaySinh, Integer id);

    boolean existsByTaiKhoan(String taiKhoan);

    boolean existsByTaiKhoanAndIdNot(String taiKhoan, Integer id);
}
