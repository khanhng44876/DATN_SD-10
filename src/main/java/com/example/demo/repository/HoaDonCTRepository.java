package com.example.demo.repository;

import com.example.demo.entity.HoaDonCT;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonCTRepository extends JpaRepository<HoaDonCT, Integer> {
    @Query("SELECT h FROM HoaDonCT h WHERE h.hoaDon.id = :hoaDonId")
    List<HoaDonCT> findByHoaDonId(@Param("hoaDonId") Integer hoaDonId);

    // Thống kê
    @Query("SELECT ct FROM HoaDonCT ct WHERE ct.hoaDon.ngayTao = :ngay")
    List<HoaDonCT> findByNgayTao(@Param("ngay") Date ngay);

    // Số lượng hàng bán trong khoảng thời gian
    @Query("SELECT SUM(hdct.soLuong) FROM HoaDonCT hdct " +
            "JOIN hdct.hoaDon hd " +
            "WHERE hd.ngayTao BETWEEN :startDate AND :endDate")
    Integer findSoldQuantityByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);



    // Top sản phẩm bán chạy
    @Query(value = """
    SELECT spct.id, spct.don_gia, spct.hinh_anh, sp.ten_san_pham, SUM(hdct.so_luong)
    FROM hoa_don_chi_tiet hdct
    JOIN san_pham_chi_tiet spct ON hdct.id_san_pham_chi_tiet = spct.id
    JOIN san_pham sp ON spct.id_san_pham = sp.id
    JOIN hoa_don hd ON hdct.id_hoa_don = hd.id
    WHERE hd.trang_thai_thanh_toan IN (N'Đã hoàn thành',N'Đã thanh toán', N'Giao hàng thành công',N'Hoàn thành')
      AND MONTH(hd.ngay_tao) = MONTH(GETDATE())
      AND YEAR(hd.ngay_tao) = YEAR(GETDATE())
    GROUP BY spct.id, spct.don_gia, spct.hinh_anh, sp.ten_san_pham
    ORDER BY SUM(hdct.so_luong) DESC
""", nativeQuery = true)
    List<Object[]> findBestSellingChiTiet(Pageable pageable);






}
