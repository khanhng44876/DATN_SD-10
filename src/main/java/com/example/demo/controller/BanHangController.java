package com.example.demo.controller;

import com.example.demo.dto.ChiTietSanPhamDto;
import com.example.demo.entity.HoaDon;
import com.example.demo.entity.HoaDonCT;
import com.example.demo.entity.KhuyenMai;
import com.example.demo.entity.SanPhamChiTiet;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
@CrossOrigin(origins = "*")
@Controller
@RequestMapping("/ban-hang-off")
public class BanHangController {
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
    HoaDonCTRepository hdCTRepository;

    @GetMapping("/hien-thi")
    public String hienThi(Model model) {
        model.addAttribute("listCTSP", ctRepository.findAll());
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

    @PostMapping("/add-hoa-don")
    public ResponseEntity<?> addHoaDon(@RequestBody HoaDon hd) {
        System.out.println("Dữ liệu nhận được: " + hd);
        HoaDon savedHoaDon = hdRepository.save(hd);
        System.out.println("Hóa đơn đã lưu: " + savedHoaDon);
        return ResponseEntity.ok(savedHoaDon);
    }

    @PostMapping("/add-hoa-don-ct")
    public ResponseEntity<HoaDonCT> addHDCT(@RequestBody HoaDonCT hdct){
        hdCTRepository.save(hdct);
        return ResponseEntity.ok(hdct);
    }

    @PutMapping("/update-sp/{id}/{so_luong}")
    public ResponseEntity<SanPhamChiTiet> updateSanPham(@PathVariable Integer id,@PathVariable Integer so_luong) {
        Optional<SanPhamChiTiet> optionalSanPham = ctRepository.findById(id);

        if (!optionalSanPham.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        SanPhamChiTiet ctSp = optionalSanPham.get();
        ctSp.setSoLuong(ctSp.getSoLuong()-so_luong);
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
}
