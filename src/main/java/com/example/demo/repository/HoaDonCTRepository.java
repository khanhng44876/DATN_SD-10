package com.example.demo.repository;

import com.example.demo.entity.HoaDonCT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HoaDonCTRepository extends JpaRepository<HoaDonCT, Integer> {
    @Query("SELECT h FROM HoaDonCT h WHERE h.hoaDon.id = :hoaDonId")
    List<HoaDonCT> findByHoaDonId(@Param("hoaDonId") Integer hoaDonId);
}
