package com.example.demo.repository;

import com.example.demo.entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    @Query("SELECT SUM(hd.tongTien) FROM HoaDon hd WHERE hd.trangThaiThanhToan IN ('Da thanh toan', 'Giao hang thanh cong') AND hd.ngayTao = :ngay")
    Double tinhDoanhThuTheoNgay(@Param("ngay") LocalDate ngay);



    @Query("SELECT SUM(hd.tongTien ) FROM HoaDon hd WHERE hd.trangThaiThanhToan IN ('Da thanh toan', 'Giao hang thanh cong') AND FUNCTION('MONTH', hd.ngayTao) = :thang AND FUNCTION('YEAR', hd.ngayTao) = :nam")
    Double tinhDoanhThuTheoThang(@Param("thang") int thang, @Param("nam") int nam);


    @Query("SELECT SUM(hd.tongTien) FROM HoaDon hd WHERE hd.trangThaiThanhToan IN ('Da thanh toan', 'Giao hang thanh cong') AND FUNCTION('YEAR', hd.ngayTao) = :nam")
    Double tinhDoanhThuTheoNam(@Param("nam") int nam);



}
