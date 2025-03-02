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
public interface KichThuocRepository extends JpaRepository<KichThuoc, Integer> {
    Optional<KichThuoc> findByTenKichThuoc(String tenKichThuoc);

    @Query("SELECT kt FROM KichThuoc kt WHERE kt.id = :idkichthuoc")
    Optional<KichThuoc> findByIdKichThuoc(@Param("idkichthuoc") Integer idkichthuoc);
    List<KichThuoc> findByTenKichThuocContainingIgnoreCase(String tenKichThuoc);
}
