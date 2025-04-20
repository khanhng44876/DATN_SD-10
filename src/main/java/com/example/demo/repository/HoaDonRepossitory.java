package com.example.demo.repository;

import com.example.demo.entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface HoaDonRepossitory extends JpaRepository<HoaDon, Integer> {
    @Query("SELECT h FROM HoaDon h WHERE (:trangThai IS NULL OR h.trangThaiThanhToan = :trangThai) ORDER BY h.id DESC")
    List<HoaDon> findByTrangThai(@Param("trangThai") String trangThai);

    List<HoaDon> findByTrangThaiThanhToanIn(List<String> trangThaiList);


    List<HoaDon> findByTrangThaiThanhToan(String trangThai);

    List<HoaDon> findAllByOrderByIdDesc();
    List<HoaDon> findByTrangThaiThanhToanOrderByIdDesc(String trangThai);

    // Tìm theo ngày cụ thể
    List<HoaDon> findByNgayTao(Date ngayTao);

    // Tìm theo tháng + năm
    @Query("SELECT h FROM HoaDon h WHERE FUNCTION('MONTH', h.ngayTao) = :thang AND FUNCTION('YEAR', h.ngayTao) = :nam")
    List<HoaDon> findByThangVaNam(@Param("thang") int thang, @Param("nam") int nam);

    // Truy vấn lọc doanh số theo tháng
        @Query("SELECT h FROM HoaDon h WHERE " +
            "FUNCTION('MONTH', h.ngayTao) = :thang AND " +
            "FUNCTION('YEAR', h.ngayTao) = :nam AND " +
            "LOWER(TRIM(h.trangThaiThanhToan)) IN :trangThais")
        List<HoaDon> findByThangNamAndTrangThaiHopLe(
        @Param("thang") int thang,
        @Param("nam") int nam,
        @Param("trangThais") List<String> trangThais
        );


    // ✅ Truy vấn dùng cho getTodaySales() lọc doanh số theo ngày
    @Query("SELECT h FROM HoaDon h WHERE h.ngayTao = :ngay AND h.trangThaiThanhToan IN :trangThais")
    List<HoaDon> findByNgayTaoAndTrangThaiThanhToan(
            @Param("ngay") Date ngay,
            @Param("trangThais") List<String> trangThais
    );
    // lấy dữ liệu từ hóa đơn cho biểu đồ quạt
    @Query("SELECT TRIM(h.trangThaiThanhToan), COUNT(h) " +
            "FROM HoaDon h " +
            "WHERE MONTH(h.ngayTao) = MONTH(CURRENT_DATE) " +
            "AND YEAR(h.ngayTao) = YEAR(CURRENT_DATE) " +
            "AND TRIM(h.trangThaiThanhToan) IN :trangThais " +
            "GROUP BY TRIM(h.trangThaiThanhToan)")
    List<Object[]> countOrderStatusThisMonth(@Param("trangThais") List<String> trangThais);



}
