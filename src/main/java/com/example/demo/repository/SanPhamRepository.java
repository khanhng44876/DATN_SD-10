package com.example.demo.repository;

import com.example.demo.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {
    @Query("SELECT h FROM SanPham h WHERE h.id = :idsp")
    Optional<SanPham> findByIdSP(@Param("idsp") Integer idsp);


// check trung khi them
    boolean existsByMaSanPham(String maSanPham);
    boolean existsByTenSanPham(String tenSanPham);

    // check trung khi update
    boolean existsByMaSanPhamAndIdNot(String maSanPham, Long id);
    boolean existsByTenSanPhamAndIdNot(String tenSanPham, Long id);



}
