package com.example.demo.service;

import com.example.demo.dto.ThongKeDTO;
import com.example.demo.entity.HoaDon;
import com.example.demo.entity.HoaDonCT;
import com.example.demo.entity.SanPhamChiTiet;
import com.example.demo.repository.HoaDonCTRepository;
import com.example.demo.repository.HoaDonRepossitory;
import com.example.demo.repository.SanPhamCTRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ThongKeService {

    @Autowired
    HoaDonCTRepository hoaDonCTRepo;

    @Autowired
    HoaDonRepossitory hoaDonRepo;

    @Autowired
    SanPhamCTRepository sanPhamChiTietRepo;

    public ThongKeDTO getThongKeTongQuat() {
        ThongKeDTO dto = new ThongKeDTO();

        dto.setDoanhSoThang(getMonthlySales());
        dto.setDoanhSoHomNay(getTodaySales());
        dto.setTongSanPhamDaBan(getMonthlySoldProductCount());
        dto.setSanPhamBanChay(getBestSellingProducts(5));
        dto.setSanPhamSapHet(getLowStockProducts(5));
        dto.setBieuDoHangNgay(getMonthlyChartData());
        dto.setTrangThaiDonHang(getOrderStatusData());

        return dto;
    }

    // Top sản phẩm bán chạy
    public List<Map<String, Object>> getBestSellingProducts(int limit) {
        List<Object[]> rawData = hoaDonCTRepo.findBestSellingChiTiet(PageRequest.of(0, limit));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rawData) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", row[0]);
            item.put("giaBan", row[1]);
            item.put("anh", row[2]);
            item.put("tenSanPham", row[3]);
            item.put("soLuongDaBan", row[4]);
            result.add(item);
        }

        return result;
    }

    // Sản phẩm sắp hết hàng
    public List<Map<String, Object>> getLowStockProducts(int threshold) {
        List<SanPhamChiTiet> list = sanPhamChiTietRepo.findBySoLuongLessThanEqual(threshold);
        List<Map<String, Object>> result = new ArrayList<>();

        for (SanPhamChiTiet ct : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("tenSanPham", ct.getSanPham().getTenSanPham());

            Map<String, Object> chiTiet = new HashMap<>();
            chiTiet.put("anh", ct.getAnhSanPham());
            chiTiet.put("gia_ban", ct.getDonGia());
            chiTiet.put("so_luong", ct.getSoLuong());

            String tenMau = (ct.getMauSac() != null) ? ct.getMauSac().getTen_mau_sac() : "Không rõ";
            String tenSize = (ct.getKichThuoc() != null) ? ct.getKichThuoc().getTenKichThuoc() : "Không rõ";

            chiTiet.put("mauSac", Map.of("ten", tenMau));
            chiTiet.put("kichCo", Map.of("ten", tenSize));

            map.put("chiTietSanPham", chiTiet);
            result.add(map);
        }

        return result;
    }


    // Tổng số sản phẩm đã bán trong tháng
    public Integer getMonthlySoldProductCount() {
        LocalDate now = LocalDate.now();
        LocalDate start = now.withDayOfMonth(1);
        LocalDate end = now;

        return hoaDonCTRepo.findSoldQuantityByDateRange(
                Date.valueOf(start), Date.valueOf(end)
        );
    }

    // Doanh số tháng hiện tại
    public Map<String, Object> getMonthlySales() {
        LocalDate now = LocalDate.now();
        int thang = now.getMonthValue();
        int nam = now.getYear();

        // ✅ Chỉ lấy đơn có trạng thái thành công/thanh toán
        List<HoaDon> hoaDons = hoaDonRepo.findThanhCongByThangVaNam(thang, nam);

        int totalOrders = hoaDons.size();
        float totalRevenue = hoaDons.stream()
                .map(HoaDon::getTongTien)
                .filter(Objects::nonNull)
                .reduce(0f, Float::sum);

        return Map.of(
                "totalOrders", totalOrders,
                "totalRevenue", totalRevenue
        );
    }



    // Doanh số hôm nay
    public Map<String, Object> getTodaySales() {
        LocalDate today = LocalDate.now();

        List<HoaDon> list = hoaDonRepo.findByNgayTaoAndTrangThaiThanhToan(
                Date.valueOf(today),  List.of("Đã thanh toán")
        );

        int tongDon = list.size();
        float tongTien = list.stream()
                .map(HoaDon::getTongTien)
                .filter(Objects::nonNull)
                .reduce(0f, Float::sum);

        return Map.of(
                "totalOrders", tongDon,
                "totalRevenue", tongTien
        );
    }


    // Biểu đồ: đơn & sản phẩm theo ngày
    public List<Map<String, Object>> getMonthlyChartData() {
        LocalDate now = LocalDate.now();
        int thang = now.getMonthValue();
        int nam = now.getYear();

        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 1; i <= now.lengthOfMonth(); i++) {
            LocalDate date = LocalDate.of(nam, thang, i);
            Date sqlDate = Date.valueOf(date);

            List<HoaDon> hoaDons = hoaDonRepo.findByNgayTao(sqlDate);
            int hoaDonCount = hoaDons.size();

            List<HoaDonCT> chiTiets = hoaDonCTRepo.findByNgayTao(sqlDate);
            int sanPhamCount = chiTiets.stream()
                    .mapToInt(HoaDonCT::getSoLuong)
                    .sum();


            Map<String, Object> map = new HashMap<>();
            map.put("date", date.toString());
            map.put("hoaDonCount", hoaDonCount);
            map.put("sanPhamCount", sanPhamCount);
            data.add(map);
        }

        return data;
    }

    // Trạng thái đơn hàng tháng này
    public Map<String, Object> getOrderStatusData() {
        List<String> labels = Arrays.asList(
                "Cho giao hang",
                "Dang giao hang",
                "Giao hang thanh cong",
                "Da huy"
        );

        // Gọi DB lấy số lượng thực tế
        List<Object[]> results = hoaDonRepo.countOrderStatusThisMonth(labels);

        // Map để chứa số lượng theo từng trạng thái
        Map<String, Integer> countMap = new HashMap<>();
        for (String label : labels) {
            countMap.put(label, 0); // mặc định 0
        }

        for (Object[] row : results) {
            String status = (String) row[0];
            Long count = (Long) row[1];
            countMap.put(status, count.intValue());
        }

        List<Integer> data = labels.stream().map(countMap::get).collect(Collectors.toList());

        Map<String, Object> trangThai = new HashMap<>();
        trangThai.put("labels", labels);
        trangThai.put("data", data);

        Map<String, Object> response = new HashMap<>();
        response.put("trangThaiDonHang", trangThai);

        return response;
    }

}
