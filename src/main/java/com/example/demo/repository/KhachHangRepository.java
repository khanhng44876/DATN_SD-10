package com.example.demo.repository;

import com.example.demo.entity.KhachHang;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {
//    Optional<KhachHang> findByTenKhachHang(@NotBlank(message = "Tên khách hàng không được để trống") String tenKhachHang);
    Optional<KhachHang> findByTaiKhoan(String taiKhoan);
//    KhachHang findBytaiKhoan(String taiKhoan);
Optional<KhachHang> findByEmail(String email);

    boolean existsByTenKhachHangAndIdNot(@NotBlank(message = "Tên khách hàng không được để trống") String tenKhachHang, Integer id);

    boolean existsByTenKhachHang(@NotBlank(message = "Tên khách hàng không được để trống") String tenKhachHang);
}
