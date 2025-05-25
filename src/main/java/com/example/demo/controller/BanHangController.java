package com.example.demo.controller;

import com.example.demo.dto.ChiTietSanPhamDto;
import com.example.demo.dto.HoaDonCT_DTO;
import com.example.demo.dto.HoaDonRequestNew;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.service.VNPAYService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.sql.SQLOutput;
import java.util.*;
@CrossOrigin(origins = "*")
@Controller
@RequestMapping("/ban-hang-off")
public class BanHangController {
    @Autowired
    private VNPAYService vnPayService;

    @Autowired
    SanPhamCTRepository ctRepository;

    @Autowired
    SanPhamRepository spRepository;

    @Autowired
    KhachHangRepository khRepository;

    @Autowired
    KhuyenMaiRepository kmRepository;

    @Autowired
    HoaDonRepossitory hdRepository;

    @Autowired
    NhanVienRepository nvRepository;

    @Autowired
    HoaDonCTRepository hdCTRepository;

    @GetMapping("/hien-thi")
    public String hienThi(Model model) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer idnv = userDetails.getId();
        List<SanPhamChiTiet> listCt = ctRepository.findAll().stream()
                        .filter(c -> c.getTrangThai().equals("Còn hàng"))
                                .toList();
        model.addAttribute("idnv", idnv);
        model.addAttribute("listCTSP", listCt);
        model.addAttribute("listKH", khRepository.findAll());
        return "/sell/index.html";
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<SanPhamChiTiet> detail(@PathVariable Integer id) {
        return ResponseEntity.ok(ctRepository.findById(id).get());
    }

    @GetMapping("/best-km/{totalAmount}")
    public ResponseEntity<Map<String,Object>> bestKm(@PathVariable Integer totalAmount) {
        KhuyenMai bestDiscount = kmRepository.findAll().stream()
                .filter(km->totalAmount >= km.getDieu_kien())
                .filter(km -> km.getTrang_thai().equals("Đang diễn ra") )
                .max(Comparator.comparing(KhuyenMai::getMuc_giam))
                .orElse(null);
        if(bestDiscount != null) {
            double finalTotalAmount = Math.min((double) (totalAmount * bestDiscount.getMuc_giam()) /100,bestDiscount.getGiam_toi_da());
            Map<String, Object> response = new HashMap<>();
            response.put("id", bestDiscount.getId());
            response.put("ma_khuyen_mai", bestDiscount.getMa_khuyen_mai());
            response.put("phan_tram_giam", bestDiscount.getMuc_giam());
            response.put("tien_giam", finalTotalAmount);
            return ResponseEntity.ok(response);

        }
        return ResponseEntity.ok(Collections.emptyMap());
    }

    @PutMapping("/reload-product-modal")
    @ResponseBody
    public List<SanPhamChiTiet> reloadProductModal() {
        List<SanPhamChiTiet> listCt = ctRepository.findAll().stream()
                .filter(c -> c.getTrangThai().equals("Còn hàng"))
                .toList();
        return listCt;
    }

    @GetMapping("/generate-qr/{amount}")
    public ResponseEntity<Map<String,String>> genQr(@PathVariable long amount, HttpServletRequest req) {
        String orderInfo = "PREVIEW " + UUID.randomUUID();
        String redirectUrl = vnPayService.createOrder(req, (int)amount, orderInfo);
        return ResponseEntity.ok(Map.of("redirectUrl", redirectUrl));
    }

    @PostMapping("/add-hoa-don")
    public ResponseEntity<?> addHoaDon(@RequestBody HoaDonRequestNew hd) {
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

        hoaDon.setKhachHang(khRepository.findById(hd.getIdKhachHang()).get());
        hoaDon.setNhanVien(nvRepository.findById(hd.getIdNhanVien()).get());
        if(hd.getIdKhuyenMai() == null) {
            hoaDon.setKhuyenMai(null);
        }else {
            hoaDon.setKhuyenMai(kmRepository.findById(hd.getIdKhuyenMai()).get());
        }

        hdRepository.save(hoaDon);
        return ResponseEntity.ok(hoaDon);
    }

    @PostMapping("/add-hoa-don-ct")
    public ResponseEntity<?> addHDCT(@RequestBody HoaDonCT_DTO hdct){
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

        hdCTRepository.save(hoaDonCT);
        return ResponseEntity.ok(hoaDonCT);
    }

    @PutMapping("/update-sp/{id}/{so_luong}")
    public ResponseEntity<SanPhamChiTiet> updateSanPham(@PathVariable Integer id,@PathVariable Integer so_luong) {
        Optional<SanPhamChiTiet> optionalSanPham = ctRepository.findById(id);

        if (optionalSanPham.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        SanPhamChiTiet ctSp = optionalSanPham.get();
        ctSp.setSoLuong(ctSp.getSoLuong()-so_luong);
        if(ctSp.getSoLuong() == 0){
            ctSp.setTrangThai("Hết hàng");
        }
        ctRepository.save(ctSp);
        return ResponseEntity.ok(ctSp);
    }

    @PutMapping("/update-km/{id}")
    public ResponseEntity<KhuyenMai> updateKM(@PathVariable Integer id) {
        Optional<KhuyenMai> optionalKhuyenMai = kmRepository.findById(id);
        if (!optionalKhuyenMai.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        KhuyenMai km = optionalKhuyenMai.get();
        km.setSo_luong(km.getSo_luong()-1);
        km.setSo_luong_sd(km.getSo_luong_sd()+1);
        kmRepository.save(km);
        return ResponseEntity.ok(km);
    }

    @PutMapping("/remove-sp/{id}/{soLuong}")
    public ResponseEntity<?> removeSp(@PathVariable Integer id,@PathVariable Integer soLuong) {
        Optional<SanPhamChiTiet> optionalSanPham = ctRepository.findById(id);
        if (optionalSanPham.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        SanPhamChiTiet ctSp = optionalSanPham.get();
        ctSp.setSoLuong(ctSp.getSoLuong()+soLuong);
        ctRepository.save(ctSp);
        return ResponseEntity.ok(ctSp);
    }
}
