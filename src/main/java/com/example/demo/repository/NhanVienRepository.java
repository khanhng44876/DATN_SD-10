package com.example.demo.repository;

import com.example.demo.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, Integer> {
//    @Query("SELECT h FROM NhanVien h WHERE h.id = :idnv")
//    Optional<NhanVien> findByIdTK(@Param("idnv") Integer idnv);
    Optional<NhanVien> findByTenDangNhap(String tenDangNhap);
    Optional<NhanVien> findByEmail(String email);

}
