package com.example.demo.controller;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
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
    private KichThuocRepository kichThuocRepository;
    @Autowired
    private ChatLieuRepository chatLieuRepository;
    @Autowired
    private MauSacRepository mauSacRepository;

    @RequestMapping("/ban-hang-online")
    public String iddd(Model model) {
        List<SanPhamChiTiet> spClb = ctsp_repository.findAll().stream()
                .filter(sp->sp.getSanPham().getDanhMuc().getId() == 1)
                .filter(sp->sp.getSanPham().getTrangThai().equals("Còn hàng"))
                .toList();
        List<SanPhamChiTiet> spDtqg = ctsp_repository.findAll().stream()
                .filter(sp->sp.getSanPham().getDanhMuc().getId() == 2)
                .filter(sp->sp.getSanPham().getTrangThai().equals("Còn hàng"))
                .limit(5)
                .toList();
        List<SanPhamChiTiet> spNologo = ctsp_repository.findAll().stream()
                .filter(sp->sp.getSanPham().getDanhMuc().getId() == 3)
                .filter(sp->sp.getSanPham().getTrangThai().equals("Còn hàng"))
                .limit(5)
                .toList();
        model.addAttribute("spDtqg",spDtqg);
        model.addAttribute("spNologo",spNologo);
        model.addAttribute("spClb", spClb);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Kiểm tra nếu user chưa đăng nhập (anonymous)
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            return "ban_hang_online/home";  // Trang chưa đăng nhập
        }

        return "ban_hang_online/index";  // Trang đã đăng nhập
    }

    @RequestMapping("/ban-hang-online/cart")
    public String idd(Model model){
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        KhachHang khachHang = khachHangRepository.findById(userDetails.getId()).orElse(null);
        model.addAttribute("kh",khachHang);
        return "ban_hang_online/giohang.html";
    }
    @RequestMapping("/ban-hang-online/sp")
    public String iddsp(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size, // 9 sản phẩm mỗi trang
            Model model) {
        Pageable pageable = (Pageable) PageRequest.of(page, size);
        Page<SanPhamChiTiet> productPage = ctsp_repository.findAll(pageable);

        model.addAttribute("listSp", productPage.getContent());
        model.addAttribute("currentPage", productPage.getNumber());
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("sizes", kichThuocRepository.findAll());
        model.addAttribute("colors", mauSacRepository.findAll());
        model.addAttribute("materials", chatLieuRepository.findAll());

        return "ban_hang_online/product.html";
    }

    @GetMapping("/ban-hang-online/sp/filter")
    public String filterProducts(
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) List<String> sizes,
            @RequestParam(required = false) List<String> colors,
            @RequestParam(required = false) List<String> materials,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<SanPhamChiTiet> productPage = ctsp_repository.filterProducts(minPrice, maxPrice, sizes, colors, materials, pageable);

        model.addAttribute("listSp", productPage.getContent());
        model.addAttribute("currentPage", productPage.getNumber());
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("sizes", kichThuocRepository.findAll());
        model.addAttribute("colors", mauSacRepository.findAll());
        model.addAttribute("materials", chatLieuRepository.findAll());

        return "ban_hang_online/product.html";
    }


    // Thêm phương thức mới để lấy chi tiết sản phẩm theo ID
//    @GetMapping("/ban-hang-online/ctsp/{id}")
//    @ResponseBody
//    public ResponseEntity<?> getProductDetail(@PathVariable("id") Integer id) {
//        try {
//            Optional<SanPhamChiTiet> optionalCtsp = ctsp_repository.findById(id);
//            if (!optionalCtsp.isPresent()) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy sản phẩm với ID: " + id);
//            }
//            SanPhamChiTiet ctsp = optionalCtsp.get();
//            return new ResponseEntity<>(ctsp, HttpStatus.OK);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi lấy chi tiết sản phẩm: " + e.getMessage());
//        }
//    }
    @GetMapping("/ban-hang-online/detail")
    public String detailPage(@RequestParam("id") Integer id, Model model) {
        Optional<SanPhamChiTiet> optionalCtsp = ctsp_repository.findById(id);
        if (!optionalCtsp.isPresent()) {
            return "ban_hang_online/error";
        }
        SanPhamChiTiet ctsp = optionalCtsp.get();
        List<SanPhamChiTiet> allDetails = ctsp_repository.findBySanPham(ctsp.getSanPham().getId());

        // Lọc trùng lặp cho kích thước
        List<SanPhamChiTiet> uniqueSizes = allDetails.stream()
                .filter(detail -> detail.getSoLuong() > 0 && "Còn hàng".equals(detail.getTrangThai()))
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                detail -> detail.getKichThuoc().getTenKichThuoc(), // Key là tên kích thước
                                detail -> detail,
                                (existing, replacement) -> existing // Giữ bản ghi đầu tiên nếu trùng
                        ),
                        map -> new ArrayList<>(map.values())
                ));

        // Lọc trùng lặp cho biến thể (màu sắc + chất liệu)
        List<SanPhamChiTiet> uniqueVariants = allDetails.stream()
                .filter(detail -> detail.getSoLuong() > 0 && "Còn hàng".equals(detail.getTrangThai()))
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                detail -> detail.getMauSac().getTen_mau_sac() + "-" + detail.getChatLieu().getTenChatLieu(), // Key là cặp màu sắc-chất liệu
                                detail -> detail,
                                (existing, replacement) -> existing // Giữ bản ghi đầu tiên nếu trùng
                        ),
                        map -> new ArrayList<>(map.values())
                ));

        model.addAttribute("product", ctsp);
        model.addAttribute("sizes", uniqueSizes); // Danh sách kích thước không trùng lặp
        model.addAttribute("variants", uniqueVariants); // Danh sách biến thể không trùng lặp
        return "ban_hang_online/ctsp";
    }


}
