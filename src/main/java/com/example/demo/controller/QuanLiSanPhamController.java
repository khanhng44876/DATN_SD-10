package com.example.demo.controller;

import com.example.demo.entity.Hang;
import com.example.demo.entity.KichThuoc;
import com.example.demo.entity.SanPham;
import com.example.demo.repository.DanhMucRepository;
import com.example.demo.repository.HangRepository;
import com.example.demo.repository.KichThuocRepository;
import com.example.demo.repository.SanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import com.example.demo.dto.SanPhamDto;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("san-pham")

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/san-pham")

public class QuanLiSanPhamController {

    @Autowired
    private SanPhamRepository sanPhamRepository;
    @Autowired
    private DanhMucRepository danhMucRepository;
    @Autowired
    private HangRepository hangRepository;
    @Autowired
    private KichThuocRepository kichThuocRepository;


    @GetMapping("hien-thi")
    public String index(Model model) {

    private ChatLieuRepository chatLieuRepository;
    @Autowired
    private HangRepository hangRepository;
    @Autowired
    private MauSacRepository mauSacRepository;
    @Autowired
    private KichThuocRepository kichThuocRepository;

    @ModelAttribute("dsMau")
    public List<MauSac> getmausac(){
        return mauSacRepository.findAll();
    }
    @ModelAttribute("dsKichThuoc")
    public List<KichThuoc> getkichthuoc(){
        return kichThuocRepository.findAll();
    }
    @ModelAttribute("dsChatLieu")
    public List<ChatLieu> getchatlieu(){
        return chatLieuRepository.findAll();
    }
    @ModelAttribute("dsDanhMuc")
    public List<DanhMuc> getdanhmuc(){return danhMucRepository.findAll();}
    @ModelAttribute("dsHang")
    public List<Hang> gethang(){return hangRepository.findAll();}


    @GetMapping("/hien-thi")
    public String index(Model model){

        List<SanPham> ds = this.sanPhamRepository.findAll();
        // Kiểm tra xem danh mục có được gán đúng không
        ds.forEach(sp -> System.out.println(sp.getDanhMuc()));
        ds.forEach(sp -> System.out.println(sp.getHang()));
        model.addAttribute("dataSP", ds);
        return "san_pham/qlsp";
    }



    //Hãng==========================================================
    @GetMapping("hang")
    public String hienThiHang(Model model) {
        List<Hang> ds = this.hangRepository.findAll();
        model.addAttribute("listHang", ds);
        return "san_pham/hang";
    }

    @GetMapping("/danh-sach-hang/{id}")
    @ResponseBody
    public ResponseEntity<Hang> dsh(@PathVariable("id") Integer idhang) {
        Optional<Hang> result = hangRepository.findByIdHang(idhang);
        return result.map(hang -> new ResponseEntity<>(hang, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/them-hang")
    public ResponseEntity<?> themHang(@RequestBody Map<String, Object> payload) {
        try {
            Hang hang = new Hang();
            hang.setTenHang((String) payload.get("tenHang"));
            hang.setTrangThai((String) payload.get("trangThai"));
            hangRepository.save(hang);
            return ResponseEntity.ok("Hãng đã được thêm thành công!");
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Lỗi: Tên hãng đã tồn tại (UNIQUE constraint).");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi thêm hãng: " + e.getMessage());
        }
    }

    @PutMapping("/cap-nhat-hang/{id}")
    public ResponseEntity<?> updatehang(@PathVariable Integer id, @RequestBody Map<String, Object> payload) {

        Hang hang = hangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hãng không tồn tại"));
        hang.setTenHang((String) payload.get("tenHang"));
        hang.setTrangThai((String) payload.get("trangThai"));
        hangRepository.save(hang);
        return ResponseEntity.ok("Hãng đã được cập nhật thành công!");
    }
    //Kích thước=========================================================
    @GetMapping("kich-thuoc")
    public String hienThiKichThuoc(Model model) {
//        List<KichThuoc> ds = this.hangRepository.findAll();
        List<KichThuoc> ds=this.kichThuocRepository.findAll();
        model.addAttribute("listKichThuoc", ds);
        return "san_pham/kichthuoc";
    }

    @GetMapping("/danh-sach-kich-thuoc/{id}")
    @ResponseBody
    public ResponseEntity<KichThuoc> dskichthuoc(@PathVariable("id") Integer idkichthuoc) {
        Optional<KichThuoc> result = kichThuocRepository.findByIdKichThuoc(idkichthuoc);
        return result.map(kichThuoc -> new ResponseEntity<>(kichThuoc, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/them-kich-thuoc")
    public ResponseEntity<?> themKichThuoc(@RequestBody Map<String, Object> payload) {
        try {
            KichThuoc kichThuoc = new KichThuoc();
            kichThuoc.setTenKichThuoc((String) payload.get("tenKichThuoc"));
            kichThuoc.setMoTa((String) payload.get("moTa"));
            kichThuocRepository.save(kichThuoc);
            return ResponseEntity.ok("Kích thước đã được thêm thành công!");
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Lỗi: Tên Kích thước đã tồn tại (UNIQUE constraint).");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi thêm Kích thước: " + e.getMessage());
        }
    }

    @PutMapping("/cap-nhat-kich-thuoc/{id}")
    public ResponseEntity<?> updatekichthuoc(@PathVariable Integer id, @RequestBody Map<String, Object> payload) {

        KichThuoc kichThuoc = kichThuocRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hãng không tồn tại"));
        kichThuoc.setTenKichThuoc((String) payload.get("tenKichThuoc"));
        kichThuoc.setMoTa((String) payload.get("moTa"));
        kichThuocRepository.save(kichThuoc);
        return ResponseEntity.ok("Kích thước đã được cập nhật thành công!");
    }
    @GetMapping("/tim-kiem-kich-thuoc")
    @ResponseBody
    public ResponseEntity<List<KichThuoc>> timKiemKichThuoc(@RequestParam("keyword") String keyword) {
        try {
            // Tìm kiếm các kích thước có tên chứa từ khóa (không phân biệt hoa thường)
            List<KichThuoc> result = kichThuocRepository.findByTenKichThuocContainingIgnoreCase(keyword);

            if (result.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT); // Trả về 204 nếu không tìm thấy kết quả
            }

            return new ResponseEntity<>(result, HttpStatus.OK); // Trả về danh sách kết quả
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // Trả về lỗi nếu có exception
        }
    }
    // Thêm sản phẩm
    @PostMapping("/them-san-pham")
    public ResponseEntity<?> themSanPham(@RequestBody SanPhamDto spDto) {
        try {
            SanPham sanPham = new SanPham();
            sanPham.setMa_san_pham(spDto.getMa_san_pham());
            sanPham.setTen_san_pham(spDto.getTen_san_pham());
            sanPham.setNgay_nhap(spDto.getNgay_nhap());
            sanPham.setNgay_sua(spDto.getNgay_sua());
            sanPham.setTrang_thai(spDto.getTrang_thai());

            // Gán danh mục (DanhMuc)
            if (spDto.getIddanhMuc() != null) {
                DanhMuc danhMuc = danhMucRepository.findById(spDto.getIddanhMuc())
                        .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));
                sanPham.setDanhMuc(danhMuc);
            }

            // Gán hãng (Hang)
            if (spDto.getIdhang() != null) {
                Hang hang = hangRepository.findById(spDto.getIdhang())
                        .orElseThrow(() -> new RuntimeException("Hãng không tồn tại"));
                sanPham.setHang(hang);
            }

            sanPhamRepository.save(sanPham);
            return ResponseEntity.ok("Sản phẩm đã được thêm thành công!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi: " + e.getMessage());
        }
    }



}
