package com.example.demo.repository;

import com.example.demo.entity.SanPhamChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SanPhamCTRepository extends JpaRepository<SanPhamChiTiet,Integer> {
    @Query("SELECT h FROM SanPhamChiTiet h WHERE h.sanPham.id = ?1")
    List<SanPhamChiTiet> findBySanPham(Integer spId);

    @Query("SELECT h FROM SanPhamChiTiet h WHERE h.sanPham.id = :idsanPham")
    List<SanPhamChiTiet> findBySanPhamId(@Param("idsanPham") Integer idsanPham);

    boolean existsBySanPhamIdAndMauSacIdAndKichThuocId(Integer sanPhamId, Integer mauSacId, Integer kichThuocId);
    boolean existsBySanPhamIdAndMauSacIdAndKichThuocIdAndIdNot(Integer idSanPham, Integer idMauSac, Integer idKichThuoc, Integer excludeId);

    // Lấy sản phẩm có số lượng <= ngưỡng
    List<SanPhamChiTiet> findBySoLuongLessThanEqual(int soLuong);

}
