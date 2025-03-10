package com.example.demo.repository;

import com.example.demo.entity.Hang;
import com.example.demo.entity.KichThuoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HangRepository extends JpaRepository<Hang,Integer> {
    Optional<Hang> findByTenHang(String tenHang);

    @Query("SELECT h FROM Hang h WHERE h.id = :idhang")
    Optional<Hang> findByIdHang(@Param("idhang") Integer idhang);
    List<Hang> findByTenHangContainingIgnoreCase(String tenHang);
}