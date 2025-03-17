package com.example.demo.controller;

import com.example.demo.entity.NhanVien;
import com.example.demo.repository.NhanVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("nhan-vien")
public class NhanVienController {
    @Autowired
    NhanVienRepository nhanVienRepository;

@GetMapping("add")
public String iii(){
    return "/nhan_vien/addNhanVien.html";
}

    @GetMapping("hien-thi")
    public String hienThiNhanVien(Model model) {
        List<NhanVien> ds = this.nhanVienRepository.findAll();
        model.addAttribute("listtk", ds);
        return "nhan_vien/nhanvien";
    }

    @GetMapping("/danh-sach-nhan-vien/{id}")
    @ResponseBody
    public ResponseEntity<NhanVien> dsmasa(@PathVariable("id") Integer idnv) {
        Optional<NhanVien> result = nhanVienRepository.findById(idnv);
        return result.map(nhanVien -> new ResponseEntity<>(nhanVien, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/them-nhan-vien")
    public ResponseEntity<?> themNV(@RequestBody Map<String, Object> payload) {
        try {
            String tenDangNhap = (String) payload.get("tenDangNhap");
            String email = (String) payload.get("email");

            // Kiểm tra xem tên đăng nhập và email đã tồn tại chưa
                if (nhanVienRepository.findByTaiKhoan(tenDangNhap).isPresent()) {
                return ResponseEntity.badRequest().body("Tên đăng nhập đã tồn tại: " + tenDangNhap);
            }
            if (nhanVienRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.badRequest().body("Email đã tồn tại: " + email);
            }

            NhanVien taiKhoan = new NhanVien();
            taiKhoan.setTaiKhoan(tenDangNhap);
            taiKhoan.setMatKhau((String) payload.get("matKhau"));
            taiKhoan.setTenNhanVien((String) payload.get("tenNhanVien"));
            taiKhoan.setChucVu((String) payload.get("chucVu"));

            taiKhoan.setEmail(email);
            taiKhoan.setSdt((String) payload.get("sdt"));

            LocalDate localDate = LocalDate.parse((String) payload.get("ngayTao"));
            taiKhoan.setNgayTao(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            taiKhoan.setNgayTao(new Date());

            taiKhoan.setTrangThai((String) payload.get("trangThai"));
            nhanVienRepository.save(taiKhoan);
            return ResponseEntity.ok("Nhân viên đã được thêm thành công!");
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Ngày tạo không đúng định dạng (yyyy-MM-dd): " + e.getMessage());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("ID chức vụ phải là số: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi server: " + e.getMessage());
        }
    }

    @PutMapping("/cap-nhat-nhan-vien/{id}")
    public ResponseEntity<?> updateNV(@PathVariable Integer id, @RequestBody Map<String, Object> payload) {
        try {
            NhanVien taiKhoan = nhanVienRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));

            String tenDangNhap = (String) payload.get("tenDangNhap");
            String email = (String) payload.get("email");

            // Kiểm tra xem tên đăng nhập và email đã tồn tại chưa
            if (!taiKhoan.getTaiKhoan().equals(tenDangNhap) && nhanVienRepository.findByTaiKhoan(tenDangNhap).isPresent()) {
                return ResponseEntity.badRequest().body("Tên đăng nhập đã tồn tại: " + tenDangNhap);
            }
            if (!taiKhoan.getEmail().equals(email) && nhanVienRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.badRequest().body("Email đã tồn tại: " + email);
            }


            taiKhoan.setTaiKhoan(tenDangNhap);
            taiKhoan.setMatKhau((String) payload.get("matKhau"));
            taiKhoan.setTenNhanVien((String) payload.get("tenNhanVien"));
            taiKhoan.setChucVu((String) payload.get("chucVu"));

            taiKhoan.setEmail(email);
            taiKhoan.setSdt((String) payload.get("sdt"));
            taiKhoan.setNgaySua(new Date());
            taiKhoan.setTrangThai((String) payload.get("trangThai"));

            nhanVienRepository.save(taiKhoan);
            return ResponseEntity.ok("Nhân viên đã được cập nhật thành công!");
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("ID chức vụ phải là số: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi server: " + e.getMessage());
        }
    }

}