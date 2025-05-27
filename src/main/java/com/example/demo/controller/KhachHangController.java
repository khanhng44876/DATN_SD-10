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
    @GetMapping("/add")
    public String hienThi(){
        return "/khach_hang/add";
    }
    @PostMapping("/them-khach-hang")
    public ResponseEntity<?> themSM(@RequestBody Map<String, Object> payload) {
        String tenKhachHang = (String) payload.get("tenKhachHang");
        String email = (String) payload.get("email");
        String soDienThoai = (String) payload.get("soDienThoai");
        LocalDate ngaySinh = LocalDate.parse((String) payload.get("ngaySinh"));

        if (khachHangRepository.existsByTenKhachHang(tenKhachHang)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Tên khách hàng đã tồn tại!");
        }

        if (khachHangRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email đã tồn tại!");
        }

        if (khachHangRepository.existsBySoDienThoai(soDienThoai)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Số điện thoại đã tồn tại!");
        }

        if (khachHangRepository.existsByNgaySinh(ngaySinh)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Ngày sinh đã tồn tại!");
        }

        KhachHang khachHang = new KhachHang();
        khachHang.setTenKhachHang(tenKhachHang);
        khachHang.setEmail(email);
        khachHang.setSoDienThoai(soDienThoai);
        khachHang.setDiaChi((String) payload.get("diaChi"));
        khachHang.setNgaySinh(ngaySinh);
        khachHang.setTaiKhoan((String) payload.get("taiKhoan"));
        khachHang.setMatKhau((String) payload.get("matKhau"));
        khachHang.setGioiTinh((String) payload.get("gioiTinh"));

        khachHangRepository.save(khachHang);
        return ResponseEntity.ok("Khách hàng đã được thêm thành công!");
    }



    @GetMapping("/update")
    public String update(){
        return "/khach_hang/update";
    }
    @PutMapping("/cap-nhat-khach-hang/{id}")
    public ResponseEntity<?> updateSM(@PathVariable Integer id, @RequestBody Map<String, Object> payload) {
        String tenKhachHang = (String) payload.get("tenKhachHang");
        String email = (String) payload.get("email");
        String soDienThoai = (String) payload.get("soDienThoai");
        LocalDate ngaySinh = LocalDate.parse((String) payload.get("ngaySinh"));

        if (khachHangRepository.existsByTenKhachHangAndIdNot(tenKhachHang, id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Tên khách hàng đã tồn tại!");
        }

        if (khachHangRepository.existsByEmailAndIdNot(email, id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email đã tồn tại!");
        }

        if (khachHangRepository.existsBySoDienThoaiAndIdNot(soDienThoai, id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Số điện thoại đã tồn tại!");
        }

        if (khachHangRepository.existsByNgaySinhAndIdNot(ngaySinh, id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Ngày sinh đã tồn tại!");
        }

        KhachHang khachHang = khachHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));

        khachHang.setTenKhachHang(tenKhachHang);
        khachHang.setEmail(email);
        khachHang.setSoDienThoai(soDienThoai);
        khachHang.setDiaChi((String) payload.get("diaChi"));
        khachHang.setNgaySinh(ngaySinh);
        khachHang.setTaiKhoan((String) payload.get("taiKhoan"));
        khachHang.setMatKhau((String) payload.get("matKhau"));
        khachHang.setGioiTinh((String) payload.get("gioiTinh"));

        khachHangRepository.save(khachHang);
        return ResponseEntity.ok("Khách hàng đã được cập nhật thành công!");
    }



}
