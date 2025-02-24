package com.example.demo.repository;

import com.example.demo.entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HoaDonRepossitory extends JpaRepository<HoaDon, Integer> {
    @Query("SELECT h FROM HoaDon h WHERE (:trangThai IS NULL OR h.trang_thai_thanh_toan = :trangThai) ORDER BY h.id DESC")
    List<HoaDon> findByTrangThai(@Param("trangThai") String trangThai);

}
