package com.example.demo.controller;

import com.example.demo.dto.ChiTietSanPhamDto;
import com.example.demo.entity.KhuyenMai;
import com.example.demo.entity.SanPhamChiTiet;
import com.example.demo.repository.KhachHangRepository;
import com.example.demo.repository.KhuyenMaiRepository;
import com.example.demo.repository.SanPhamCTRepository;
import com.example.demo.repository.SanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

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
}
