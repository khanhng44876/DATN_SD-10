package com.example.demo.controller;

import com.example.demo.entity.Hang;
import com.example.demo.entity.KichThuoc;
import com.example.demo.repository.HangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.List;

@Controller
@RequestMapping("/san-pham")
public class HangController {

    @Autowired
    private HangRepository hangRepository;

    @GetMapping("/hang/save-hang")
    public String hienthiPageAdd() {
        return "/san_pham/addHang.html";
    }
    @GetMapping("/hang") // Đổi từ hien-thi thành /hang để khớp với HTML reload
    public String hienThiHang(Model model) {
        List<Hang> ds = this.hangRepository.findAll();
        model.addAttribute("listHang", ds);
        return "san_pham/hang";
    }

    @GetMapping("/danh-sach-hang/{id}")
    @ResponseBody
    public ResponseEntity<Hang> dsh(@PathVariable("id") Integer idhang) {
        Optional<Hang> result = hangRepository.findById(idhang); // Giả sử findByIdHang là findById
        return result.map(hang -> new ResponseEntity<>(hang, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Sửa chức năng Thêm
    @PostMapping("/them-hang")
    @ResponseBody
    public ResponseEntity<String> themHang(@RequestBody Hang hang) {
        try {
            hangRepository.save(hang);
            return ResponseEntity.ok("Hãng đã được thêm thành công!");
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Lỗi: Tên hãng đã tồn tại.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi thêm hãng: " + e.getMessage());
        }
    }

    // Sửa chức năng Cập nhật
    @PutMapping("/cap-nhat-hang/{id}")
    @ResponseBody
    public ResponseEntity<String> capNhatHang(@PathVariable("id") Integer id, @RequestBody Hang hang) {
        Optional<Hang> existingHang = hangRepository.findById(id);
        if (!existingHang.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Hãng không tồn tại.");
        }
        try {
            Hang updatedHang = existingHang.get();
            updatedHang.setTenHang(hang.getTenHang());
            updatedHang.setTrangThai(hang.getTrangThai());
            hangRepository.save(updatedHang);
            return ResponseEntity.ok("Hãng đã được cập nhật thành công!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi cập nhật hãng: " + e.getMessage());
        }
    }

}