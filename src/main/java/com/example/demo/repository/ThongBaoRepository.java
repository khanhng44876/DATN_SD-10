package com.example.demo.repository;

import com.example.demo.entity.ThongBao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThongBaoRepository extends JpaRepository<ThongBao, Integer> {

    List<ThongBao> findByKhachHang_IdOrderByReadAscNgayTaoDesc(Integer khachHangId);

    Long countByKhachHang_IdAndReadFalse(Integer khachHangId);
}
