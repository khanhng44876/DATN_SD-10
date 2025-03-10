package com.example.demo.controller;

import com.example.demo.entity.KichThuoc;
import com.example.demo.repository.KichThuocRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/san-pham")
public class KichThuocController {

    @Autowired
    private KichThuocRepository kichThuocRepository;

    @GetMapping("/kich-thuoc") // Đổi từ /hien-thi thành /kich-thuoc để khớp với HTML
    public String hienThiKichThuoc(Model model) {
        List<KichThuoc> ds = this.kichThuocRepository.findAll();
        model.addAttribute("listKichThuoc", ds);
        return "san_pham/kichthuoc"; // Đảm bảo template này khớp với HTML
    }

    @GetMapping("/danh-sach-kich-thuoc/{id}")
    @ResponseBody
    public ResponseEntity<KichThuoc> getKichThuocById(@PathVariable("id") Integer id) {
        Optional<KichThuoc> result = kichThuocRepository.findById(id); // Giả sử findByIdKichThuoc đổi thành findById nếu dùng JPA chuẩn
        return result.map(kichThuoc -> ResponseEntity.ok(kichThuoc))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // Sửa chức năng Thêm
    @PostMapping("/them-kich-thuoc")
    @ResponseBody
    public ResponseEntity<String> themKichThuoc(@RequestBody KichThuoc kichThuoc) {
        try {
            kichThuocRepository.save(kichThuoc);
            return ResponseEntity.ok("Kích thước đã được thêm thành công!");
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Lỗi: Tên kích thước đã tồn tại.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi thêm kích thước: " + e.getMessage());
        }
    }

    // Sửa chức năng Cập nhật
    @PutMapping("/cap-nhat-kich-thuoc/{id}")
    @ResponseBody
    public ResponseEntity<String> capNhatKichThuoc(@PathVariable("id") Integer id, @RequestBody KichThuoc kichThuoc) {
        Optional<KichThuoc> existingKichThuoc = kichThuocRepository.findById(id);
        if (!existingKichThuoc.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Kích thước không tồn tại.");
        }
        try {
            KichThuoc updatedKichThuoc = existingKichThuoc.get();
            updatedKichThuoc.setTenKichThuoc(kichThuoc.getTenKichThuoc());
            updatedKichThuoc.setMoTa(kichThuoc.getMoTa());
            kichThuocRepository.save(updatedKichThuoc);
            return ResponseEntity.ok("Kích thước đã được cập nhật thành công!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi cập nhật kích thước: " + e.getMessage());
        }
    }

    // Giữ nguyên chức năng tìm kiếm
    @GetMapping("/tim-kiem-kich-thuoc")
    @ResponseBody
    public ResponseEntity<List<KichThuoc>> timKiemKichThuoc(@RequestParam("keyword") String keyword) {
        try {
            List<KichThuoc> result = kichThuocRepository.findByTenKichThuocContainingIgnoreCase(keyword);
            if (result.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}