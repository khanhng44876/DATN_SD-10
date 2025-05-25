package com.example.demo.repository;

import com.example.demo.entity.SanPham;
import com.example.demo.entity.SanPhamChiTiet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SanPhamCTRepository extends JpaRepository<SanPhamChiTiet,Integer> {
    @Query("SELECT h FROM SanPhamChiTiet h WHERE h.sanPham.id = ?1")
    List<SanPhamChiTiet> findBySanPham(Integer spId);

    @Query("SELECT h FROM SanPhamChiTiet h WHERE h.sanPham.id = :idsanPham")
    List<SanPhamChiTiet> findBySanPhamId(@Param("idsanPham") Integer idsanPham);

    boolean existsBySanPhamIdAndMauSacIdAndKichThuocIdAndChatLieuId(
            Integer sanPhamId, Integer mauSacId, Integer kichThuocId, Integer chatLieuId
    );

    boolean existsBySanPhamIdAndMauSacIdAndKichThuocIdAndChatLieuIdAndIdNot(
            Integer sanPhamId, Integer mauSacId, Integer kichThuocId, Integer chatLieuId, Integer excludeId
    );


    // Lấy sản phẩm có số lượng <= ngưỡng
    List<SanPhamChiTiet> findBySoLuongLessThanEqual(int soLuong);


    @Query("SELECT spct FROM SanPhamChiTiet spct " +
            "WHERE (:minPrice IS NULL OR spct.donGia >= :minPrice) " +
            "AND (:maxPrice IS NULL OR spct.donGia <= :maxPrice) " +
            "AND (:sizes IS NULL OR spct.kichThuoc.tenKichThuoc IN :sizes) " +
            "AND (:colors IS NULL OR spct.mauSac.ten_mau_sac IN :colors) " +
            "AND (:materials IS NULL OR spct.chatLieu.tenChatLieu IN :materials)")
    Page<SanPhamChiTiet> filterProducts(
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("sizes") List<String> sizes,
            @Param("colors") List<String> colors,
            @Param("materials") List<String> materials,
            Pageable pageable);
}
