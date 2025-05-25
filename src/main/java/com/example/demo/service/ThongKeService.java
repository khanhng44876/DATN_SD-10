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
        List<SanPhamChiTiet> list = sanPhamChiTietRepo.findBySoLuongLessThanEqual(threshold)
                .stream()
                .filter(ct -> ct.getSoLuong() > 0)
                .collect(Collectors.toList());

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

        Integer soldCount = hoaDonCTRepo.findSoldQuantityByDateRange(Date.valueOf(start), Date.valueOf(end));
        return (soldCount != null) ? soldCount : 0;
    }

    // Doanh số tháng hiện tại
    public Map<String, Object> getMonthlySales() {
        LocalDate now = LocalDate.now();
        int thang = now.getMonthValue();
        int nam = now.getYear();

        // Chuẩn hóa trạng thái hợp lệ
        List<String> trangThaiHopLe = List.of("đã thanh toán", "hoàn thành", "tại quầy", "da thanh toan,Da thanh toan", "Đã hoàn thành")
                .stream()
                .map(s -> s.trim().toLowerCase())
                .collect(Collectors.toList());

        // Query với trạng thái đã chuẩn hóa
        List<HoaDon> hoaDons = hoaDonRepo.findByThangNamAndTrangThaiHopLe(thang, nam, trangThaiHopLe);

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
                Date.valueOf(today), List.of("đã thanh toán", "hoàn thành", "tại quầy", "da thanh toan,Da thanh toan", "Đã hoàn thành")
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

            List<HoaDon> hoaDons = hoaDonRepo.findByNgayTao(sqlDate).stream()
                    .filter(hd -> !"Đã huỷ".equalsIgnoreCase(hd.getTrangThaiThanhToan()))
                    .collect(Collectors.toList());
            int hoaDonCount = hoaDons.size();

            List<HoaDonCT> chiTiets = hoaDonCTRepo.findByNgayTao(sqlDate).stream()
                    .filter(ct -> ct.getHoaDon() != null && !"Đã huỷ".equalsIgnoreCase(ct.getHoaDon().getTrangThaiThanhToan()))
                    .collect(Collectors.toList());

            int sanPhamCount = chiTiets.stream()
                    .map(HoaDonCT::getSoLuong)
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
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
        // Danh sách hiển thị cố định cho biểu đồ
        List<String> labels = Arrays.asList(
                "Chờ xác nhận",
                "Chờ giao hàng",
                "Đang giao hàng",
                "Giao hàng thành công",
                "Hoàn thành",
                "Đã hủy"
        );

        // Mapping trạng thái trong DB -> nhãn cố định trong labels
        Map<String, String> statusMapping = Map.of(
                "Chua thanh toan", "Chờ xác nhận",
                "Da thanh toan", "Hoàn thành",
                "Đã hoàn thành", "Hoàn thành",
                "Dang giao", "Đang giao hàng",
                "Giao thanh cong", "Giao hàng thành công",
                "Cho giao", "Chờ giao hàng",
                "Da huy", "Đã hủy"
                // Thêm nếu DB có khác nữa
        );

        // Mặc định mỗi trạng thái có 0 đơn
        Map<String, Integer> countMap = new LinkedHashMap<>();
        for (String label : labels) {
            countMap.put(label, 0);
        }

        // Lấy tất cả đơn hàng tháng này
        List<Object[]> results = hoaDonRepo.countOrderStatusThisMonthAll();

        for (Object[] row : results) {
            String rawStatus = ((String) row[0]).trim();
            Long count = (Long) row[1];

            // Ánh xạ về label cố định (nếu có)
            String mappedStatus = statusMapping.getOrDefault(rawStatus, rawStatus);

            // Chỉ cộng nếu trạng thái đó có trong labels
            if (countMap.containsKey(mappedStatus)) {
                countMap.put(mappedStatus, count.intValue());
            }
        }

        List<Integer> data = labels.stream()
                .map(countMap::get)
                .collect(Collectors.toList());

        Map<String, Object> trangThai = new HashMap<>();
        trangThai.put("labels", labels);
        trangThai.put("data", data);

        return Map.of("trangThaiDonHang", trangThai);
    }

}
