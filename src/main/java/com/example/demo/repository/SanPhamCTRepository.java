package com.example.demo.repository;

import com.example.demo.entity.SanPhamChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SanPhamCTRepository extends JpaRepository<SanPhamChiTiet,Integer> {
    @Query("SELECT h FROM SanPhamChiTiet h WHERE h.sanPham.id = :sanPhamId")
    List<SanPhamChiTiet> findBySanPhamId(@Param("sanPhamId") Integer sanPhamId);
}
