package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.exception.MessageException;
import com.example.demo.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Controller

public class BanHangOnlineController {
    @Autowired
    private SanPhamRepository spRepository;
    @Autowired
    private SanPhamCTRepository ctsp_repository;
    @Autowired
    private KhachHangRepository khachHangRepository;
    @Autowired
    private HoaDonRepossitory hoaDonrepo;
    @Autowired
    private HoaDonCTRepository hdctRepository;
    @Autowired
    private DanhMucRepository danhMucRepository;
    @Autowired
    private NhanVienRepository nhanVienRepository;
    @Autowired
    private KhuyenMaiRepository khuyenMaiRepository;
    @Autowired
    private SimpMessagingTemplate simMessagingTemplate;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    // Bán hàng online cua admin
    @RequestMapping("/ban-hang-online/admin")
    public String hienThi(Model model) {
        List<HoaDon> listhd = hoaDonrepo.findAll().stream()
                .filter(hd -> hd.getTrangThaiThanhToan().equals("Chờ xác nhận"))
                .toList();
        List<HoaDon> listhd2 = hoaDonrepo.findAll().stream()
                .filter(hd -> hd.getTrangThaiThanhToan().equals("Chờ giao hàng"))
                .toList();
        List<HoaDon> listhd3 = hoaDonrepo.findAll().stream()
                .filter(hd -> hd.getTrangThaiThanhToan().equals("Đang giao hàng"))
                .toList();
        List<HoaDon> listhd4 = hoaDonrepo.findAll().stream()
                .filter(hd -> hd.getTrangThaiThanhToan().equals("Giao hàng thành công"))
                .toList();
        List<HoaDon> listhd5 = hoaDonrepo.findAll().stream()
                .filter(hd -> hd.getTrangThaiThanhToan().equals("Hoàn thành"))
                .toList();
        List<HoaDon> listhd6 = hoaDonrepo.findAll().stream()
                .filter(hd -> hd.getTrangThaiThanhToan().equals("Đã hủy"))
                .toList();
        model.addAttribute("listhd", listhd);
        model.addAttribute("listhd2", listhd2);
        model.addAttribute("listhd3", listhd3);
        model.addAttribute("listhd4", listhd4);
        model.addAttribute("listhd5", listhd5);
        model.addAttribute("listhd6", listhd6);
        return "ban_hang_online/adminHome.html";
    }

    // vào trang lshd để cập nhật trạng thái đơn hàng của admin
    @RequestMapping("/ban-hang-online/lshd/{id}")
    public String showLshd(Model model, @PathVariable int id) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        NhanVien nhanVien = nhanVienRepository.findById(userDetails.getId()).orElse(null);
        HoaDon hoaDon = hoaDonrepo.findById(id).get();
        hoaDon.setNhanVien(nhanVien);
        System.out.println(hoaDon);
        model.addAttribute("hdNCommit", hoaDon);
        List<HoaDonCT> listhdct = hdctRepository.findByHoaDonId(id);
        model.addAttribute("listhdct", listhdct);
        System.out.println(hoaDon);
        System.out.println(listhdct);
        return "ban_hang_online/lshd.html";
    }

    // Trang sản phẩm ca Customer
    @RequestMapping("/ban-hang-online")
    public String iddd(Model model) {
        List<SanPhamChiTiet> spClb = ctsp_repository.findAll().stream()
                .filter(sp -> sp.getSanPham().getDanhMuc().getId() == 1)
                .filter(sp -> sp.getSanPham().getTrangThai().equals("Còn hàng"))
                .toList();
        List<SanPhamChiTiet> spDtqg = ctsp_repository.findAll().stream()
                .filter(sp -> sp.getSanPham().getDanhMuc().getId() == 2)
                .filter(sp -> sp.getSanPham().getTrangThai().equals("Còn hàng"))
                .limit(5)
                .toList();
        List<SanPhamChiTiet> spNologo = ctsp_repository.findAll().stream()
                .filter(sp -> sp.getSanPham().getDanhMuc().getId() == 3)
                .filter(sp -> sp.getSanPham().getTrangThai().equals("Còn hàng"))
                .limit(5)
                .toList();
        model.addAttribute("spDtqg", spDtqg);
        model.addAttribute("spNologo", spNologo);
        model.addAttribute("spClb", spClb);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Kiểm tra nếu user chưa đăng nhập (anonymous)
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            return "ban_hang_online/home";  // Trang chưa đăng nhập
        }

        return "ban_hang_online/index";  // Trang đã đăng nhập
    }

    // trang giỏ hàng
    @RequestMapping("/ban-hang-online/cart")
    public String idd(Model model) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        KhachHang khachHang = khachHangRepository.findById(userDetails.getId()).orElse(null);
        model.addAttribute("kh", khachHang);
        List<KhuyenMai> listKm = khuyenMaiRepository.findAll().stream()
                        .filter(k->k.getTrang_thai().equals("Đang diễn ra"))
                                .toList();
        model.addAttribute("listKm",listKm);
        return "ban_hang_online/giohang.html";
    }

    // trang hiển thị danh sách đơn hàng của customer
    @RequestMapping("/ban-hang-online/dsdh-customer")
    public String iddsdh(Model model) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        KhachHang khachHang = khachHangRepository.findById(userDetails.getId()).orElse(null);
        List<HoaDon> listhd = hoaDonrepo.findAll().stream()
                .filter(hd -> hd.getKhachHang().getId() == khachHang.getId())
                .toList();
        List<Map<String, Object>> listHoaDonData = new ArrayList<>();
        for (HoaDon hd : listhd) {
            Map<String, Object> obj = new HashMap<>();
            obj.put("hoaDon", hd);
            obj.put("chiTiet", hdctRepository.findByHoaDonId(hd.getId()));
            listHoaDonData.add(obj);
        }

        model.addAttribute("listhd", listHoaDonData);
        model.addAttribute("kh", khachHang);
        return "ban_hang_online/dsdh-customer.html";
    }

    // trang này để theo dõi đơn hàng của Customer
    @RequestMapping("/ban-hang-online/follow-order/{id}")
    public String followOrder(@PathVariable Integer id, Model model) {
        HoaDon hoaDon = hoaDonrepo.findById(id).get();
        model.addAttribute("hd", hoaDon);
        return "ban_hang_online/customer.html";
    }

    // Hủy hóa đơn
    @PutMapping("/ban-hang-online/cancel-order/{id}")
    public ResponseEntity<HoaDon> cancelOrder(@PathVariable Integer id) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        HoaDon hd = hoaDonrepo.findById(id).get();
        if (hd == null) return ResponseEntity.notFound().build();
        if ("Chờ xác nhận".equals(hd.getTrangThaiThanhToan())) {
            hd.setTrangThaiThanhToan("Đã hủy");
            hoaDonrepo.save(hd);

            // Gửi thông báo cho client qua WebSocket
            boolean isKhachHang = userDetails.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("KHACH_HANG"));

            boolean isNhanVienOrQL = userDetails.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("NHAN_VIEN") || auth.getAuthority().equals("QUAN_LY"));

            // Gửi socket đến người còn lại
            if (isNhanVienOrQL && hd.getKhachHang() != null) {
                simpMessagingTemplate.convertAndSendToUser(
                        hd.getKhachHang().getTaiKhoan(),
                        "/topic/order/" + id,
                        "Đã hủy"
                );
            }

            if (isKhachHang && hd.getNhanVien() != null) {
                simpMessagingTemplate.convertAndSendToUser(
                        hd.getNhanVien().getTaiKhoan(),
                        "/topic/order/" + id,
                        "Đã hủy"
                );
            }

            return ResponseEntity.ok(hd);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }


    @MessageMapping("/update-quantity")
    public void handleUpdateQuantity(Map<String, Object> data) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer totalAmount = Integer.valueOf(data.get("totalAmount").toString());
        Integer orderId = Integer.valueOf(data.get("orderId").toString());
        Integer itemId = Integer.valueOf(data.get("itemId").toString());
        Integer quantity = Integer.valueOf(data.get("quantity").toString());

        // Xử lý logic
        HoaDon hd = hoaDonrepo.findById(orderId).get();
        HoaDonCT ct = hdctRepository.findById(itemId).get();
        hd.setTongTien(Float.valueOf(totalAmount));
        ct.setSoLuong(quantity);
        ct.setThanhTien(ct.getSoLuong() * ct.getDonGia());
        ct.setTongTien(ct.getSoLuong() * ct.getDonGia());

        boolean isKhachHang = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("KHACH_HANG"));

        boolean isNhanVienOrQL = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("NHAN_VIEN") || auth.getAuthority().equals("QUAN_LY"));

        // Gửi socket đến người còn lại
        if (isNhanVienOrQL && hd.getKhachHang() != null) {
            simpMessagingTemplate.convertAndSendToUser(
                    hd.getKhachHang().getTaiKhoan(),
                    "/topic/order/" + orderId, Map.of(
                            "type", "update-quantity",
                            "itemId", itemId,
                            "quantity", quantity,
                            "totalAmount", totalAmount
                    )

            );
        }

        if (isKhachHang && hd.getNhanVien() != null) {
            simpMessagingTemplate.convertAndSendToUser(
                    hd.getNhanVien().getTaiKhoan(),
                    "/topic/order/" + orderId,
                    Map.of(
                            "type","update-quantity",
                            "itemId", itemId,
                            "quantity", quantity,
                            "totalAmount", totalAmount
                    )
            );
        }
    }

    @PostMapping("/ban-hang-online/create-order")
    @Transactional
    public ResponseEntity<?> createOrder(@RequestBody HoaDonRequestNew hd) {
        System.out.println("Dữ liệu nhận được: " + hd);
        HoaDon hoaDon = new HoaDon();
        hoaDon.setNgayTao(hd.getNgayTao());
        hoaDon.setNgaySua(hd.getNgaySua());
        hoaDon.setDonGia(hd.getDonGia());
        hoaDon.setTongTien(hd.getTongTien());
        hoaDon.setTrangThaiThanhToan(hd.getTrangThaiThanhToan());
        hoaDon.setHinhThucThanhToan(hd.getHinhThucThanhToan());
        hoaDon.setDiaChiGiaoHang(hd.getDiaChiGiaoHang());
        hoaDon.setGhiChu(hd.getGhiChu());

        hoaDon.setKhachHang(khachHangRepository.findById(hd.getIdKhachHang()).get());
        hoaDon.setNhanVien(nhanVienRepository.findById(hd.getIdNhanVien()).get());
        if (hd.getIdKhuyenMai() == null) {
            hoaDon.setKhuyenMai(null);
        } else {
            hoaDon.setKhuyenMai(khuyenMaiRepository.findById(hd.getIdKhuyenMai()).get());
        }

        hoaDonrepo.save(hoaDon);

        return ResponseEntity.ok(hoaDon);
    }

    @PostMapping("/ban-hang-online/create-order-ct")
    @Transactional
    public ResponseEntity<?> createOrderCT(@RequestBody HoaDonCT_DTO hdct) {
        System.out.println(hdct);
        HoaDonCT hoaDonCT = new HoaDonCT();
        hoaDonCT.setDonGia(hdct.getDonGia());
        hoaDonCT.setSoLuong(hdct.getSoLuong());
        hoaDonCT.setTrangThai(hdct.getTrangThai());
        hoaDonCT.setTongTien(hdct.getTongTien());
        hoaDonCT.setThanhTien(hdct.getThanhTien());
        hoaDonCT.setNgayTao(hdct.getNgayTao());
        hoaDonCT.setNgaySua(hdct.getNgaySua());

        hoaDonCT.setHoaDon_id(hdct.getIdHoaDon());
        hoaDonCT.setCtsp_id(hdct.getIdSanPhamChiTiet());

        hdctRepository.save(hoaDonCT);
        return ResponseEntity.ok(hoaDonCT);
    }

    @PutMapping("/ban-hang-online/updateHD/{id}/{status}")
    public ResponseEntity<HoaDon> updateHD(@PathVariable Integer id, @PathVariable String status) {
        HoaDon hd = hoaDonrepo.findById(id).get();
        if (status.equals("Chờ xác nhận")) {
            hd.setTrangThaiThanhToan("Chờ giao hàng");
        }
        if (status.equals("Chờ giao hàng")) {
            hd.setTrangThaiThanhToan("Đang giao hàng");
        }
        if (status.equals("Đang giao hàng")) {
            hd.setTrangThaiThanhToan("Giao hàng thành công");
        }
        if (status.equals("Giao hàng thành công")) {
            hd.setTrangThaiThanhToan("Hoàn thành");
        }

        String newStatus = hd.getTrangThaiThanhToan();
        hoaDonrepo.save(hd);
        simpMessagingTemplate.convertAndSendToUser(
                hd.getKhachHang().getTaiKhoan(),     // username (chính là getUsername())
                "/topic/order/" + id,                // sẽ trở thành /user/queue/order/{id}
                newStatus
        );
        return ResponseEntity.ok(hd);
    }

    @PutMapping("/ban-hang-online/updateCT/{id}/{status}")
    public ResponseEntity<HoaDonCT> updateCT(@PathVariable Integer id, @PathVariable String status) {
        HoaDonCT ct = hdctRepository.findById(id).get();
        if (status.equals("Chờ xác nhận")) {
            ct.setTrangThai("Chờ giao hàng");
        }
        if (status.equals("Chờ giao hàng")) {
            ct.setTrangThai("Đang giao hàng");
        }
        if (status.equals("Đang giao hàng")) {
            ct.setTrangThai("Giao hàng thành công");
        }
        if (status.equals("Giao hàng thành công")) {
            ct.setTrangThai("Hoàn thành");
        }
        hdctRepository.save(ct);
        return ResponseEntity.ok(ct);
    }

    @PutMapping("/ban-hang-online/update-sp/{id}/{so_luong}")
    public ResponseEntity<SanPhamChiTiet> updateSP(@PathVariable Integer id, @PathVariable Integer so_luong) {
        SanPhamChiTiet ct = ctsp_repository.findById(id).get();
        ct.setSoLuong(ct.getSoLuong() - so_luong);
        ctsp_repository.save(ct);
        return ResponseEntity.ok(ct);
    }

    //Khi Tổng tiền hàng thay đôi thì gọi cái này
    @GetMapping("/ban-hang-online/best-km/{totalAmount}")
    public ResponseEntity<Map<String,Object>> bestKm(@PathVariable Integer totalAmount) {
        KhuyenMai bestDiscount = khuyenMaiRepository.findAll().stream()
                .filter(km->totalAmount >= km.getDieu_kien())
                .filter(km -> km.getTrang_thai().equals("Đang diễn ra") )
                .max(Comparator.comparing(KhuyenMai::getMuc_giam))
                .orElse(null);
        if(bestDiscount != null) {
            double finalTotalAmount = Math.min((double) (totalAmount * bestDiscount.getMuc_giam()) /100,bestDiscount.getGiam_toi_da());
            Map<String, Object> response = new HashMap<>();
            response.put("id", bestDiscount.getId());
            response.put("ten_khuyen_mai", bestDiscount.getTen_khuyen_mai());
            response.put("phan_tram_giam", bestDiscount.getMuc_giam());
            response.put("dieu_kien",bestDiscount.getDieu_kien());
            response.put("giam_toi_da",bestDiscount.getGiam_toi_da());
            response.put("tien_giam", finalTotalAmount);
            return ResponseEntity.ok(response);

        }
        return ResponseEntity.ok(Collections.emptyMap());
    }
}
