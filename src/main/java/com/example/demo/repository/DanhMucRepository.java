package com.example.demo.repository;

import com.example.demo.entity.DanhMuc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DanhMucRepository extends JpaRepository<DanhMuc,Integer> {
    @Query("SELECT d FROM DanhMuc d WHERE d.tendanhmuc = :tenDanhMuc")
    Optional<DanhMuc> findByTenDanhMuc(@Param("tenDanhMuc") String tenDanhMuc);



    @Query("SELECT h FROM DanhMuc h WHERE h.id = :iddm")
    Optional<DanhMuc> findByIdDm(@Param("iddm") Integer iddm);
}
