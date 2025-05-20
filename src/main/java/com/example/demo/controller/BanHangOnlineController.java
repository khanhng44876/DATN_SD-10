package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.exception.MessageException;
import com.example.demo.repository.*;
import com.example.demo.service.VNPAYService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.security.Principal;
import java.util.*;


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
    private SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private HoaDonCTRepository hoaDonCTRepository;
    @Autowired
    private VNPAYService vnPayService;
    @Autowired
    private ThongBaoRepository thongBaoRepository;

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
        hoaDonrepo.save(hoaDon);
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

    // trang này để Customer theo dõi đơn hàng của mình
    @RequestMapping("/ban-hang-online/follow-order/{id}")
    public String followOrder(@PathVariable Integer id, Model model,Authentication authentication) {
        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();
        KhachHang kh = khachHangRepository.findById(userDetails.getId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User không hợp lệ"));

        HoaDon hoaDon = hoaDonrepo.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Hoá đơn không tồn tại"));

        Map<String, Object> hoaDonMap = new HashMap<>();
        hoaDonMap.put("hoaDon", hoaDon);
        hoaDonMap.put("chiTiet",hdctRepository.findByHoaDonId(hoaDon.getId()));
        model.addAttribute("kh", kh);
        model.addAttribute("hoadonMap", hoaDonMap);
        return "ban_hang_online/customer.html";
    }

    // Hủy hóa đơn
    @PutMapping("/ban-hang-online/cancel-order/{id}")
    public ResponseEntity<HoaDon> cancelOrder(@PathVariable Integer id,Principal principal) {
        CustomUserDetails userDetails =
                (CustomUserDetails) ((Authentication) principal).getPrincipal();
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

            Map<String, Object> payload = Map.of(
                    "type",        "status-cancel",
                    "status","Đã hủy"
            );
            KhuyenMai km = hd.getKhuyenMai();
            km.setSo_luong(km.getSo_luong()+1);
            km.setSo_luong_sd(km.getSo_luong_sd()-1);
            khuyenMaiRepository.save(km);
            // Gửi socket đến người còn lại
            if (isNhanVienOrQL && hd.getKhachHang() != null) {
                simpMessagingTemplate.convertAndSendToUser(
                        hd.getKhachHang().getTaiKhoan(),
                        "/topic/order/" + id,
                        payload
                );
            }

            if (isKhachHang && hd.getNhanVien() != null) {
                simpMessagingTemplate.convertAndSendToUser(
                        hd.getNhanVien().getTaiKhoan(),
                        "/topic/order/" + id,
                        payload
                );
            }

            return ResponseEntity.ok(hd);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }


    @Transactional
    @MessageMapping("/update-quantity")
    public void handleUpdateQuantity(
            @Payload Map<String, Object> data,
            Principal principal
    ) {
        CustomUserDetails userDetails =
                (CustomUserDetails) ((Authentication) principal).getPrincipal();
        // 1. Parse dữ liệu
        Integer orderId     = Integer.valueOf(data.get("orderId").toString());
        Integer itemId      = Integer.valueOf(data.get("itemId").toString());
        Integer quantity    = Integer.valueOf(data.get("quantity").toString());
        Integer totalAmount = Integer.valueOf(data.get("totalAmount").toString());

        // 2. Lấy ra cập nhật vao entity
        HoaDon hd = hoaDonrepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid orderId: " + orderId));
        HoaDonCT ct = hdctRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid itemId: " + itemId));

        ct.setSoLuong(quantity);
        ct.setThanhTien(ct.getSoLuong() * ct.getDonGia());
        ct.setTongTien(ct.getThanhTien());
        hdctRepository.save(ct);

        hd.setTongTien(totalAmount.floatValue());
        hoaDonrepo.save(hd);

        // 3. Xác định vai trò người gửi
        boolean isKhachHang = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("KHACH_HANG"));
        boolean isNVorQL    = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("NHAN_VIEN") || a.getAuthority().equals("QUAN_LY"));

        // 4. Gửi message real‑time cho bên còn lại
        Map<String, Object> payload = Map.of(
                "type",        "update-quantity",
                "orderId",     orderId,
                "itemId",      itemId,
                "quantity",    quantity,
                "totalAmount", totalAmount
        );
        System.out.println(hd.getNhanVien());
        if (isNVorQL && hd.getKhachHang() != null) {
            simpMessagingTemplate.convertAndSendToUser(
                    hd.getKhachHang().getTaiKhoan(),
                    "/topic/order/" + orderId,
                    payload
            );
        } else if (isKhachHang && hd.getNhanVien() != null) {
            simpMessagingTemplate.convertAndSendToUser(
                    hd.getNhanVien().getTaiKhoan(),
                    "/topic/order/" + orderId,
                    payload
            );
        }
    }

    @PostMapping("/ban-hang-online/create-order")
    @Transactional
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody HoaDonRequestNew hd, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (hd.getIdKhachHang() == null || hd.getIdNhanVien() == null) {
                response.put("error", "Thiếu ID khách hàng hoặc nhân viên");
                return ResponseEntity.badRequest().body(response);
            }

            HoaDon hoaDon = new HoaDon();
            hoaDon.setNgayTao(hd.getNgayTao() != null ? hd.getNgayTao() : new Date());
            hoaDon.setNgaySua(hd.getNgaySua());
            hoaDon.setDonGia(hd.getDonGia());
            hoaDon.setTongTien(hd.getTongTien());
            hoaDon.setTrangThaiThanhToan(hd.getTrangThaiThanhToan());
            hoaDon.setHinhThucThanhToan(hd.getHinhThucThanhToan());
            hoaDon.setDiaChiGiaoHang(hd.getDiaChiGiaoHang());
            hoaDon.setGhiChu(hd.getGhiChu());

            if (hd.getIdKhuyenMai() != null) {
                KhuyenMai km = khuyenMaiRepository.findById(hd.getIdKhuyenMai())
                        .orElseThrow(() -> new EntityNotFoundException("KM không tồn tại"));
                if (km.getSo_luong() <= 0) {
                    throw new IllegalStateException("Khuyến mãi đã hết số lượng");
                }
                km.setSo_luong(km.getSo_luong() - 1);
                km.setSo_luong_sd(km.getSo_luong_sd()+1);
                if(km.getSo_luong() == 0){
                    km.setTrang_thai("Đã kết thúc");
                }
                khuyenMaiRepository.save(km);

                hoaDon.setKhuyenMai(km);
            }
            return khachHangRepository.findById(hd.getIdKhachHang())
                    .map(khachHang -> {
                        hoaDon.setKhachHang(khachHang);
                        return nhanVienRepository.findById(hd.getIdNhanVien())
                                .map(nhanVien -> {
                                    hoaDon.setNhanVien(nhanVien);
                                    if (hd.getIdKhuyenMai() != null) {
                                        khuyenMaiRepository.findById(hd.getIdKhuyenMai())
                                                .ifPresent(hoaDon::setKhuyenMai);
                                    }
                                    HoaDon savedHoaDon = hoaDonrepo.save(hoaDon);

                                    ThongBao thongBao = new ThongBao();
                                    thongBao.setLink("/ban-hang-online/follow-order/"+savedHoaDon.getId());
                                    thongBao.setNoi_dung("Đơn hàng HD"+savedHoaDon.getId()+"của bạn đã được đặt thành công. Đang trong trạng thái: Chờ xác nhận ");
                                    thongBao.setNgayTao(new Date());
                                    thongBao.setKhachHang(savedHoaDon.getKhachHang());
                                    thongBaoRepository.save(thongBao);
                                    simpMessagingTemplate.convertAndSendToUser(
                                            savedHoaDon.getKhachHang().getTaiKhoan(),
                                            "/topic/notification",
                                            thongBaoRepository.findByKhachHang_IdOrderByReadAscNgayTaoDesc(savedHoaDon.getKhachHang().getId())
                                    );
                                    response.put("id", savedHoaDon.getId());
                                    if ("Online".equals(hd.getHinhThucThanhToan())) {
                                        String orderInfo = "Thanh toan don hang " + savedHoaDon.getId() + " cua KH: " + hd.getIdKhachHang();
                                        String vnpayUrl = vnPayService.createOrder(request, savedHoaDon.getTongTien().intValue(), orderInfo);
                                        response.put("redirectUrl", vnpayUrl);
                                    }
                                    return ResponseEntity.ok(response);
                                })
                                .orElseGet(() -> {
                                    response.put("error", "Nhân viên không tồn tại");
                                    return ResponseEntity.badRequest().body(response);
                                });
                    })
                    .orElseGet(() -> {
                        response.put("error", "Khách hàng không tồn tại");
                        return ResponseEntity.badRequest().body(response);
                    });
        } catch (Exception e) {
            response.put("error", "Lỗi server: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/ban-hang-online/create-order-ct")
    @Transactional
    public ResponseEntity<?> createOrderCT(@RequestBody HoaDonCT_DTO hdct) {
        try {
            if (hdct.getIdHoaDon() == null || hdct.getIdSanPhamChiTiet() == null) {
                return ResponseEntity.badRequest().body("Thiếu ID hóa đơn hoặc sản phẩm chi tiết");
            }

            HoaDonCT hoaDonCT = new HoaDonCT();
            hoaDonCT.setDonGia(hdct.getDonGia());
            hoaDonCT.setSoLuong(hdct.getSoLuong());
            hoaDonCT.setTrangThai(hdct.getTrangThai());
            hoaDonCT.setTongTien(hdct.getTongTien());
            hoaDonCT.setThanhTien(hdct.getThanhTien());
            hoaDonCT.setNgayTao(hdct.getNgayTao() != null ? hdct.getNgayTao() : new Date());
            hoaDonCT.setNgaySua(hdct.getNgaySua());
            hoaDonCT.setHoaDon_id(hdct.getIdHoaDon());
            hoaDonCT.setCtsp_id(hdct.getIdSanPhamChiTiet());

            HoaDonCT savedHoaDonCT = hoaDonCTRepository .save(hoaDonCT);
            return ResponseEntity.ok(savedHoaDonCT);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi server: " + e.getMessage());
        }
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
        if(status.equals("Hoàn thành")){
            hd.setTrangThaiThanhToan("Đã hoàn thành");
        }
        String newStatus = hd.getTrangThaiThanhToan();
        hoaDonrepo.save(hd);
        ThongBao thongBao = new ThongBao();
        thongBao.setNgayTao(new Date());
        thongBao.setLink("/ban-hang-online/follow-order/"+hd.getId());
        thongBao.setNoi_dung("Đơn hàng HD"+hd.getId()+"của bạn đang trong trạng thái: "+hd.getTrangThaiThanhToan());
        thongBao.setKhachHang(hd.getKhachHang());
        thongBaoRepository.save(thongBao);

        Map<String, Object> payload = Map.of(
                "type",        "update-status",
                "status", newStatus
                );
        simpMessagingTemplate.convertAndSendToUser(
                hd.getKhachHang().getTaiKhoan(),     // username (chính là getUsername())
                "/topic/order/" + id,                // sẽ trở thành /user/queue/order/{id}
                payload
        );
        simpMessagingTemplate.convertAndSendToUser(
                hd.getKhachHang().getTaiKhoan(),
                "/topic/notification",
                thongBaoRepository.findByKhachHang_IdOrderByReadAscNgayTaoDesc(hd.getKhachHang().getId())
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

    @PutMapping("/read-noti/{id}")
    public ResponseEntity<ThongBao> readNoti(@PathVariable Integer id) {
        ThongBao tb = thongBaoRepository.findById(id).get();
        tb.setRead(true);
        thongBaoRepository.save(tb);
        return ResponseEntity.ok(tb);
    }
}
