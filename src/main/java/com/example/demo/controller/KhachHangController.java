package com.example.demo.controller;

import com.example.demo.entity.KhachHang;
import com.example.demo.repository.KhachHangRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("khach-hang")
public class KhachHangController {
@Autowired
    private KhachHangRepository khachHangRepository;
    @GetMapping("hien-thi")
    public String index(HttpSession session, Model model) {
        List<KhachHang> ls = khachHangRepository.findAll();
        model.addAttribute("listkh", ls);
        return "khach_hang/index";
    }

    @GetMapping("/danh-sach-khach-hang/{id}")
    @ResponseBody
    public ResponseEntity<KhachHang> dsmasa(@PathVariable("id") Integer idkh) {
        Optional<KhachHang> result = khachHangRepository.findById(idkh);
        return result.map(mauSac -> new ResponseEntity<>(mauSac, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/them-khach-hang")
    public ResponseEntity<?> themSM(@RequestBody Map<String, Object> payload) {
        KhachHang khachHang = new KhachHang();
        khachHang.setTenKhachHang((String) payload.get("tenKhachHang"));
        khachHang.setEmail((String) payload.get("email"));
        khachHang.setSoDienThoai((String) payload.get("soDienThoai"));
        khachHang.setDiaChi((String) payload.get("diaChi"));
        khachHang.setNgaySinh(LocalDate.parse((String) payload.get("ngaySinh")));
        khachHang.setTaiKhoan((String) payload.get("taiKhoan"));
        khachHang.setMatKhau((String) payload.get("matKhau"));
        khachHang.setGioiTinh((String) payload.get("gioiTinh"));
        khachHangRepository.save(khachHang);
        return ResponseEntity.ok("Khách hàng đã được thêm thành công!");
    }

    @PutMapping("/cap-nhat-khach-hang/{id}")
    public ResponseEntity<?> updateSM(@PathVariable Integer id, @RequestBody Map<String, Object> payload) {

        KhachHang khachHang= khachHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khach hang không tồn tại"));
        khachHang.setTenKhachHang((String) payload.get("tenKhachHang"));
        khachHang.setEmail((String) payload.get("email"));
        khachHang.setSoDienThoai((String) payload.get("soDienThoai"));
        khachHang.setDiaChi((String) payload.get("diaChi"));
        khachHang.setNgaySinh(LocalDate.parse((String) payload.get("ngaySinh")));
        khachHang.setTaiKhoan((String) payload.get("taiKhoan"));
        khachHang.setMatKhau((String) payload.get("matKhau"));
        khachHang.setGioiTinh((String) payload.get("gioiTinh"));
        khachHangRepository.save(khachHang);
        return ResponseEntity.ok("Khách hàng đã được cập nhật thành công!");
    }

}
