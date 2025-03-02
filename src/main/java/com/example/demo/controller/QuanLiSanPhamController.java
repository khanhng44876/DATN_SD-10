package com.example.demo.controller;

import com.example.demo.dto.SanPhamDto;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/san-pham")
public class QuanLiSanPhamController {

    @Autowired
     private SanPhamRepository sanPhamRepository;
    @Autowired
    private DanhMucRepository danhMucRepository;
    @Autowired
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
//    danhMuc===============================================================
    @GetMapping("chat-lieu")
    public String chatlieu(Model model) {
        List<ChatLieu> ds = this.chatLieuRepository.findAll();
        model.addAttribute("listct", ds);
        return "san_pham/chatlieu";
    }

    @GetMapping("/danh-sach-chat-lieu/{id}")
    @ResponseBody
    public ResponseEntity<ChatLieu> dsmasaaa(@PathVariable("id") Integer idct) {
        Optional<ChatLieu> result = chatLieuRepository.findByIdCt(idct);
        return result.map(mauSac -> new ResponseEntity<>(mauSac, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/them-chat-lieu")
    public ResponseEntity<?> themScM(@RequestBody Map<String, Object> payload) {
        ChatLieu chatLieu = new ChatLieu();
        chatLieu.setTenChatLieu((String) payload.get("tenChatLieu"));
        chatLieu.setMoTa((String) payload.get("moTa"));
        chatLieuRepository.save(chatLieu);
        return ResponseEntity.ok("Chất liệu đã được thêm thành công!");
    }

    @PutMapping("/cap-nhat-chat-lieu/{id}")
    public ResponseEntity<?> updateSaM(@PathVariable Integer id, @RequestBody Map<String, Object> payload) {

        ChatLieu chatLieu = chatLieuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chất liệu không tồn tại"));
        chatLieu.setTenChatLieu((String) payload.get("tenChatLieu"));
        chatLieu.setMoTa((String) payload.get("moTa"));
        chatLieuRepository.save(chatLieu);
        return ResponseEntity.ok("Chất liệu đã được cập nhật thành công!");
    }

    @GetMapping("danh-muc")
    public String listddh(Model model) {
        List<DanhMuc> ds = this.danhMucRepository.findAll();
        model.addAttribute("listdm", ds);
        return "san_pham/danhmuc";
    }

    @GetMapping("/danh-sach-danh-muc/{id}")
    @ResponseBody
    public ResponseEntity<DanhMuc> dsmasasss(@PathVariable("id") Integer iddm) {
        Optional<DanhMuc> result = danhMucRepository.findByIdDm(iddm);
        return result.map(danhMuc -> new ResponseEntity<>(danhMuc, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/them-danh-muc")
    public ResponseEntity<?> themdm(@RequestBody Map<String, Object> payload) {
        DanhMuc danhMuc = new DanhMuc();
        danhMuc.setTendanhmuc((String) payload.get("tenDanhMuc"));
        danhMuc.setMota((String) payload.get("moTa"));
        danhMucRepository.save(danhMuc);
        return ResponseEntity.ok("Danh muc đã được thêm thành công!");
    }

    @PutMapping("/cap-nhat-danh-muc/{id}")
    public ResponseEntity<?> updateDM(@PathVariable Integer id, @RequestBody Map<String, Object> payload) {

        DanhMuc danhMuc = danhMucRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Danh muc không tồn tại"));
        danhMuc.setTendanhmuc((String) payload.get("tenDanhMuc"));
        danhMuc.setMota((String) payload.get("moTa"));
        danhMucRepository.save(danhMuc);
        return ResponseEntity.ok("Danh muc đã được thêm thành công!");
    }


}
