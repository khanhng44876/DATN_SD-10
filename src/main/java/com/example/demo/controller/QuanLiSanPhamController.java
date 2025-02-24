package com.example.demo.controller;

import com.example.demo.entity.Hang;
import com.example.demo.entity.KichThuoc;
import com.example.demo.entity.SanPham;
import com.example.demo.repository.DanhMucRepository;
import com.example.demo.repository.HangRepository;
import com.example.demo.repository.KichThuocRepository;
import com.example.demo.repository.SanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("san-pham")
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
        List<SanPham> ds = this.sanPhamRepository.findAll();
        // Kiểm tra xem danh mục có được gán đúng không
        ds.forEach(sp -> System.out.println(sp.getDanhMuc()));
        ds.forEach(sp -> System.out.println(sp.getHang()));
        model.addAttribute("dataSP", ds);
        return "san_pham/qlsp";
    }

    @GetMapping("kich-thuoc")
    public String size(Model model){
        List<KichThuoc> ds = this.kichThuocRepository.findAll();
        model.addAttribute("listsize",ds);
        return "san_pham/kichthuoc";
    }
    @GetMapping("hang")
    public String hang(Model model){
        List<Hang> ds = this.hangRepository.findAll();
        model.addAttribute("listhang",ds);
        return "san_pham/hang";
    }




//    @GetMapping("/danh-sach-kich-thuoc/{id}")
//    @ResponseBody
//    public ResponseEntity<KichThuoc> dsmasaky(@PathVariable("id") Integer idkt) {
//        Optional<KichThuoc> result = sizeRepository.findByIdSize(idkt);
//        return result.map(mauSac -> new ResponseEntity<>(mauSac, HttpStatus.OK))
//                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
//    }
//
//    @PostMapping("/them-kich-thuoc")
//    public ResponseEntity<?> themKT(@RequestBody Map<String, Object> payload) {
//        Size size = new Size();
//        size.setTenSize((String) payload.get("tenSize"));
//        size.setMoTa((String) payload.get("moTa"));
//        sizeRepository.save(size);
//        return ResponseEntity.ok("Size đã được thêm thành công!");
//    }
//
//    @PutMapping("/cap-nhat-kich-thuoc/{id}")
//    public ResponseEntity<?> updatekt(@PathVariable Integer id, @RequestBody Map<String, Object> payload) {
//
//        Size size = sizeRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Size không tồn tại"));
//        size.setTenSize((String) payload.get("tenSize"));
//        size.setMoTa((String) payload.get("moTa"));
//        sizeRepository.save(size);
//        return ResponseEntity.ok("Size đã được cập nhật thành công!");
//    }

}
