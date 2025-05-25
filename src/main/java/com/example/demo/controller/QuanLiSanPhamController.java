package com.example.demo.controller;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import com.example.demo.dto.SanPhamDto;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@CrossOrigin(origins = "*")  // Hỗ trợ gọi API từ Frontend khác (React, Vue)
@Controller
@RequestMapping("/san-pham")
public class QuanLiSanPhamController {

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private SanPhamCTRepository sanPhamCTRepository;
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
    @ModelAttribute("dataSP")
    public List<SanPham> getSanPham() {
        return sanPhamRepository.findAll();
    }


    @GetMapping("/addsp")
    public String iii(){
        return "san_pham/addsp";
    }
    @GetMapping("/addctsp/{id}")
    public String iiie(){
        return "san_pham/them_ctsp";
    }

    // Hiển thị danh danh sách sản phẩm
    @GetMapping("/hien-thi")
    public String index(Model model){
        List<SanPham> ds = this.sanPhamRepository.findAll();
        // Kiểm tra xem danh mục có được gán đúng không
        ds.forEach(sp -> System.out.println(sp.getDanhMuc()));
        ds.forEach(sp -> System.out.println(sp.getHang()));
        model.addAttribute("dataSP", ds);
        return "san_pham/qlsp";
    }

    // Lấy danh sách sản phẩm
    @GetMapping("/danh-sach-san-pham/{id}")
    @ResponseBody
    public ResponseEntity<SanPham> dssan(@PathVariable("id") Integer idsp) {
        Optional<SanPham> result = sanPhamRepository.findByIdSP(idsp);
        return result.map(sanPham -> new ResponseEntity<>(sanPham, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/loc-san-pham")
    @ResponseBody
    public List<SanPham> locSanPham(
            @RequestParam(required = false) String maSanPham,
            @RequestParam(required = false) String tenSanPham,
            @RequestParam(required = false) String ngayNhap,
            @RequestParam(required = false) String ngaySua,
            @RequestParam(required = false) Integer iddanhMuc,
            @RequestParam(required = false) Integer idhang,
            @RequestParam(required = false) String trangThai) {

        List<SanPham> danhSach = sanPhamRepository.findAll();

        // Lọc dữ liệu
        if (maSanPham != null && !maSanPham.isEmpty()) {
            danhSach = danhSach.stream()
                    .filter(sp -> sp.getMaSanPham().toLowerCase().contains(maSanPham.toLowerCase()))
                    .toList();
        }
        if (tenSanPham != null && !tenSanPham.isEmpty()) {
            danhSach = danhSach.stream()
                    .filter(sp -> sp.getTenSanPham().toLowerCase().contains(tenSanPham.toLowerCase()))
                    .toList();
        }
        if (ngayNhap != null && !ngayNhap.isEmpty()) {
            danhSach = danhSach.stream()
                    .filter(sp -> sp.getNgayNhap().toString().equals(ngayNhap))
                    .toList();
        }
        if (ngaySua != null && !ngaySua.isEmpty()) {
            danhSach = danhSach.stream()
                    .filter(sp -> sp.getNgaySua().toString().equals(ngaySua))
                    .toList();
        }
        if (iddanhMuc != null) {
            danhSach = danhSach.stream()
                    .filter(sp -> sp.getDanhMuc() != null && sp.getDanhMuc().getId().equals(iddanhMuc))
                    .toList();
        }
        if (idhang != null) {
            danhSach = danhSach.stream()
                    .filter(sp -> sp.getHang() != null && sp.getHang().getId().equals(idhang))
                    .toList();
        }
        if (trangThai != null && !trangThai.isEmpty()) {
            danhSach = danhSach.stream()
                    .filter(sp -> sp.getTrangThai().equalsIgnoreCase(trangThai))
                    .toList();
        }

        return danhSach;
    }



    // Thêm sản phẩm
    @PostMapping("/them-san-pham")
    public ResponseEntity<?> themSanPham(@RequestBody SanPhamDto spDto, RedirectAttributes redirectAttributes) {
        try {
            SanPham sanPham = new SanPham();
            sanPham.setMaSanPham(spDto.getMaSanPham());
            sanPham.setTenSanPham(spDto.getTenSanPham());
            sanPham.setNgayNhap(spDto.getNgayNhap());
            sanPham.setNgaySua(spDto.getNgayNhap());
            sanPham.setTrangThai(spDto.getTrangThai());

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



    @PutMapping("/cap-nhat-san-pham/{id}")
    public ResponseEntity<?> updateSanPham(@PathVariable Integer id, @RequestBody Map<String, Object> payload) {
        // Tìm sản phẩm cần cập nhật dựa trên ID
        SanPham sanPham = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        // Cập nhật các trường của sản phẩm
        sanPham.setMaSanPham((String) payload.get("maSanPham"));
        sanPham.setTenSanPham((String) payload.get("tenSanPham"));

        // Cập nhật trường ngày nhập, kiểm tra lỗi định dạng
        try {
            sanPham.setNgayNhap(LocalDate.parse((String) payload.get("ngayNhap")));
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Định dạng ngày nhập không hợp lệ");
        }

        // Cập nhật trường ngày sửa, kiểm tra lỗi định dạng
        try {
            sanPham.setNgaySua(LocalDate.parse((String) payload.get("ngaySua")));
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body("Định dạng ngày nhập không hợp lệ");
        }

        // Kiểm tra và cập nhật danh mục
        Object danhMucObj = payload.get("iddanhMuc");
        if (danhMucObj == null || danhMucObj.toString().isEmpty()) {
            return ResponseEntity.badRequest().body("Danh mục không được để trống");
        }

        Integer iddanhMuc;
        try {
            iddanhMuc = Integer.parseInt(danhMucObj.toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("ID danh mục không hợp lệ");
        }

        DanhMuc danhMuc = danhMucRepository.findById(iddanhMuc)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));
        sanPham.setDanhMuc(danhMuc);

        // Kiểm tra và cập nhật hãng
        Object hangObj = payload.get("idhang");
        if (hangObj == null || hangObj.toString().isEmpty()) {
            return ResponseEntity.badRequest().body("Hãng không được để trống");
        }

        Integer idhang;
        try {
            idhang = Integer.parseInt(hangObj.toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("ID hãng không hợp lệ");
        }

        Hang hang = hangRepository.findById(idhang)
                .orElseThrow(() -> new RuntimeException("Hãng không tồn tại"));
        sanPham.setHang(hang);


        // Cập nhật trạng thái
        sanPham.setTrangThai((String) payload.get("trangThai"));

        // Lưu sản phẩm đã cập nhật vào cơ sở dữ liệu
        sanPhamRepository.save(sanPham);

        return ResponseEntity.ok("Sản phẩm đã được cập nhật thành công!");
    }


    //     Hiển thị danh sách chi tiết sản phẩm
    @GetMapping("ds-ctsp/{id}")
    public String HienThi (@PathVariable("id") Integer id, Model model){
        List<SanPhamChiTiet> ctsps = this.sanPhamCTRepository.findBySanPhamId(id);
        model.addAttribute("listctsp", ctsps);
        return "san_pham/ctsp";
    }





    @PostMapping("/them-ctsp")
    @ResponseBody
    public ResponseEntity<?> themCtsp(
            @RequestParam("idsanPham") Integer idsanPham,
            @RequestParam("idmauSac") Integer idmauSac,
            @RequestParam("idkichThuoc") Integer idkichThuoc,
            @RequestParam("idchatLieu") Integer idchatLieu,
            @RequestParam("soLuong") Integer soLuong,
            @RequestParam("donGia") Float donGia,
            @RequestParam("moTa") String moTa,
            @RequestParam("trangThai") String trangThai,
            @RequestParam(value = "anhSanPham", required = false) MultipartFile anhSanPham) {

        try {
            SanPham sanPham = sanPhamRepository.findById(idsanPham)
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

            MauSac mauSac = mauSacRepository.findById(idmauSac)
                    .orElseThrow(() -> new RuntimeException("Màu sắc không tồn tại!"));
            KichThuoc size = kichThuocRepository.findById(idkichThuoc)
                    .orElseThrow(() -> new RuntimeException("Kích thước không tồn tại!"));
            ChatLieu chatLieu = chatLieuRepository.findById(idchatLieu)
                    .orElseThrow(() -> new RuntimeException("Chất liệu không tồn tại!"));

            SanPhamChiTiet ctsp = new SanPhamChiTiet();
            ctsp.setSanPham(sanPham);
            ctsp.setMauSac(mauSac);
            ctsp.setKichThuoc(size);
            ctsp.setChatLieu(chatLieu);
            ctsp.setSoLuong(soLuong);
            ctsp.setDonGia(donGia);
            ctsp.setMoTa(moTa);
            ctsp.setTrangThai(trangThai);

            if (anhSanPham != null && !anhSanPham.isEmpty()) {
                String uploadDir = "D:\\DATN_SD-10\\src\\main\\webapp\\images";
                String originalName = anhSanPham.getOriginalFilename();
                String fileExt = originalName.substring(originalName.lastIndexOf(".")); // ví dụ: .webp, .jpg
                String randomFileName = UUID.randomUUID().toString() + fileExt;

                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                try (InputStream inputStream = anhSanPham.getInputStream()) {
                    Path filePath = uploadPath.resolve(randomFileName);
                    Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                    ctsp.setAnhSanPham(randomFileName);
                }
            } else {
                ctsp.setAnhSanPham(null);
            }


            sanPhamCTRepository.save(ctsp);
            return ResponseEntity.ok("Thêm chi tiết sản phẩm thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi: " + e.getMessage());
        }
    }
    // Check trung ma va ten sản phẩm

    @GetMapping("/check-ma-san-pham")
    @ResponseBody
    public boolean checkMaSanPham(@RequestParam String maSanPham) {
        return sanPhamRepository.existsByMaSanPham(maSanPham);
    }

    @GetMapping("/check-ten-san-pham")
    @ResponseBody
    public boolean checkTenSanPham(@RequestParam String tenSanPham) {
        return sanPhamRepository.existsByTenSanPham(tenSanPham);
    }

    // check trung update san pham
    @GetMapping("/check-ma-trung-update")
    @ResponseBody
    public boolean checkMaUpdate(@RequestParam String maSanPham, @RequestParam Long id) {
        return sanPhamRepository.existsByMaSanPhamAndIdNot(maSanPham, id);
    }

    @GetMapping("/check-ten-trung-update")
    @ResponseBody
    public boolean checkTenUpdate(@RequestParam String tenSanPham, @RequestParam Long id) {
        return sanPhamRepository.existsByTenSanPhamAndIdNot(tenSanPham, id);
    }




    // check trung san pham chi tiet
    @GetMapping("/kiem-tra-trung-ctsp")
    public ResponseEntity<Boolean> kiemTraTrungCtsp(
            @RequestParam("idSanPham") Integer idSanPham,
            @RequestParam("idMauSac") Integer idMauSac,
            @RequestParam("idKichThuoc") Integer idKichThuoc,
            @RequestParam("idChatLieu") Integer idChatLieu,
            @RequestParam(value = "excludeId", required = false) Integer excludeId
    ) {
        boolean exists;
        if (excludeId == null) {
            exists = sanPhamCTRepository.existsBySanPhamIdAndMauSacIdAndKichThuocIdAndChatLieuId(
                    idSanPham, idMauSac, idKichThuoc, idChatLieu
            );
        } else {
            exists = sanPhamCTRepository.existsBySanPhamIdAndMauSacIdAndKichThuocIdAndChatLieuIdAndIdNot(
                    idSanPham, idMauSac, idKichThuoc, idChatLieu, excludeId
            );
        }
        return ResponseEntity.ok(exists);
    }







    @PutMapping("/cap-nhat-ctsp/{id}")
    public ResponseEntity<?> updateCTSP(
            @PathVariable Integer id,
            @RequestParam("idsanPham") Integer idsanPham,
            @RequestParam("idmauSac") Integer idmauSac,
            @RequestParam("idkichThuoc") Integer idkichThuoc,
            @RequestParam("idchatLieu") Integer idchatLieu,
            @RequestParam("soLuong") Integer soLuong,
            @RequestParam("donGia") Float donGia,
            @RequestParam("moTa") String moTa,
            @RequestParam("trangThai") String trangThai,
            @RequestParam(value = "anhSanPham", required = false) MultipartFile anhSanPham) {

        try {
            SanPhamChiTiet ctsp = sanPhamCTRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Chi tiết sản phẩm không tồn tại!"));

            SanPham sanPham = sanPhamRepository.findById(idsanPham)
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

            MauSac mauSac = mauSacRepository.findById(idmauSac)
                    .orElseThrow(() -> new RuntimeException("Màu sắc không tồn tại!"));

            KichThuoc kichThuoc = kichThuocRepository.findById(idkichThuoc)
                    .orElseThrow(() -> new RuntimeException("Kích thước không tồn tại!"));

            ChatLieu chatLieu = chatLieuRepository.findById(idchatLieu)
                    .orElseThrow(() -> new RuntimeException("Chất liệu không tồn tại!"));

            // Set giá trị mới
            ctsp.setSanPham(sanPham);
            ctsp.setMauSac(mauSac);
            ctsp.setKichThuoc(kichThuoc);
            ctsp.setChatLieu(chatLieu);
            ctsp.setSoLuong(soLuong);
            ctsp.setDonGia(donGia);
            ctsp.setMoTa(moTa);
            ctsp.setTrangThai(trangThai);

            // Xử lý ảnh nếu có
            if (anhSanPham != null && !anhSanPham.isEmpty()) {
                String uploadDir = "D:\\DATN_SD-10\\src\\main\\webapp\\images";
                String originalName = anhSanPham.getOriginalFilename();
                String fileExt = originalName.substring(originalName.lastIndexOf(".")); // giữ đuôi ảnh
                String randomFileName = UUID.randomUUID().toString() + fileExt;

                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                try (InputStream inputStream = anhSanPham.getInputStream()) {
                    Path filePath = uploadPath.resolve(randomFileName);
                    Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                    ctsp.setAnhSanPham(randomFileName);
                }
            }

            sanPhamCTRepository.save(ctsp);
            return ResponseEntity.ok("Cập nhật chi tiết sản phẩm thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi: " + e.getMessage());
        }
    }






    @GetMapping("/chinh-sua-ctsp/{id}")
    public String chinhSuaCTSP(@PathVariable("id") Integer id, Model model) {
        SanPhamChiTiet ctsp = sanPhamCTRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));

        model.addAttribute("ctsp", ctsp);
        model.addAttribute("dataSP", sanPhamRepository.findAll());
        model.addAttribute("dsMau", mauSacRepository.findAll());
        model.addAttribute("dsKichThuoc", kichThuocRepository.findAll());
        model.addAttribute("dsChatLieu", chatLieuRepository.findAll());

        return "san_pham/sua_ctsp"; // Trả về trang HTML sửa chi tiết sản phẩm
    }







    //    danhMuc===============================================================
    @GetMapping("chat-lieu")
    public String chatlieu(Model model) {
        List<ChatLieu> ds = this.chatLieuRepository.findAll();
        model.addAttribute("listct", ds);
        return "san_pham/chatlieu";
    }

    @GetMapping("/chat-lieu/addcl")
    public String hienThi(){
        return "/san_pham/addcl";
    }
    @GetMapping("/chat-lieu/updatecl")
    public String update(){
        return "/san_pham/updatecl";
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

    @GetMapping("/danh-muc/adddm")
    public String hienThi2(){
        return "/san_pham/adddm";
    }

    @GetMapping("/danh-muc/updatedm")
    public String update2(){
        return "/san_pham/updatedm";
    }
    @GetMapping("/danh-sach-danh-muc/{id}")
    @ResponseBody
    public ResponseEntity<DanhMuc> dsmasasss(@PathVariable("id") Integer iddm) {
        Optional<DanhMuc> result = danhMucRepository.findByIdDm(iddm);
        return result.map(danhMuc -> new ResponseEntity<>(danhMuc, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    // check trùng danh mục
    @GetMapping("/kiem-tra-trung-danh-muc")
    public ResponseEntity<Map<String, Boolean>> kiemTraTrungChiTiet(
            @RequestParam String tenDanhMuc,
            @RequestParam String moTa,
            @RequestParam(required = false) Integer id
    ) {
        Map<String, Boolean> result = new HashMap<>();
        boolean tenTrung = false;
        boolean moTaTrung = false;

        Optional<DanhMuc> tenDanhMucExist = danhMucRepository.findByTendanhmuc(tenDanhMuc);
        if (tenDanhMucExist.isPresent() && (id == null || !tenDanhMucExist.get().getId().equals(id))) {
            tenTrung = true;
        }

        Optional<DanhMuc> moTaExist = danhMucRepository.findByMota(moTa);
        if (moTaExist.isPresent() && (id == null || !moTaExist.get().getId().equals(id))) {
            moTaTrung = true;
        }

        result.put("tenTrung", tenTrung);
        result.put("moTaTrung", moTaTrung);

        return ResponseEntity.ok(result);
    }


    @PostMapping("/them-danh-muc")
    public ResponseEntity<?> themdm(@RequestBody Map<String, Object> payload) {
        DanhMuc danhMuc = new DanhMuc();
        danhMuc.setTendanhmuc((String) payload.get("tenDanhMuc"));
        danhMuc.setMota((String) payload.get("moTa"));
        danhMuc.setTrangThai((String) payload.get("trangThai"));
        danhMucRepository.save(danhMuc);
        return ResponseEntity.ok("Danh muc đã được thêm thành công!");
    }

    @PutMapping("/cap-nhat-danh-muc/{id}")
    public ResponseEntity<?> updateDM(@PathVariable Integer id, @RequestBody Map<String, Object> payload) {
        DanhMuc danhMuc = danhMucRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));

        danhMuc.setTendanhmuc((String) payload.get("tenDanhMuc"));
        danhMuc.setMota((String) payload.get("moTa"));
        danhMuc.setTrangThai((String) payload.get("trangThai"));

        danhMucRepository.save(danhMuc);
        return ResponseEntity.ok("Cập nhật thành công");
    }


    @GetMapping("/chinh-sua/{id}")
    public String hienThiFormChinhSua(@PathVariable Integer id, Model model) {
        Optional<SanPham> sanPham = sanPhamRepository.findById(id);
        if (sanPham.isPresent()) {
            model.addAttribute("sanPham", sanPham.get());
            return "san_pham/updatesp"; // Trả về trang chỉnh sửa
        } else {
            return "redirect:/san-pham/hien-thi"; // Nếu không tìm thấy, quay về trang danh sách
        }
    }


}
