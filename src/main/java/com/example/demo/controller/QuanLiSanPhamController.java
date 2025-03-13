package com.example.demo.controller;

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
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    @GetMapping("/addsp")
    public String iii(){
        return "san_pham/addsp";
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

    @GetMapping("/check-ma-san-pham")
    @ResponseBody
    public boolean checkMaSanPham(@RequestParam String maSanPham) {
        return sanPhamRepository.findByMaSanPham(maSanPham).isPresent();
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


    // Hiển thị danh sách chi tiết sản phẩm
    @GetMapping("ds-ctsp/{id}")
    public String HienThi (@PathVariable("id") Integer id, Model model){
        List<SanPhamChiTiet> ctsps = this.sanPhamCTRepository.findBySanPhamId(id);
        model.addAttribute("listctsp", ctsps);
        return "san_pham/ctsp";
    }

    @GetMapping("/danh-sach-ctsp/{id}")
    @ResponseBody
    public ResponseEntity<SanPhamChiTiet> dsctsp(@PathVariable("id") Integer idctsp) {
        Optional<SanPhamChiTiet> result = sanPhamCTRepository.findById(idctsp);
        return result.map(ctsp -> new ResponseEntity<>(ctsp, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    @PostMapping("/them-ctsp")
    public String themCtsp(
            @RequestParam("idsanPham") Integer idsanPham,
            @RequestParam("idmauSac") Integer idmauSac,
            @RequestParam("idkichThuoc") Integer idkichThuoc,
            @RequestParam("idchatLieu") Integer idchatLieu,
            @RequestParam("soLuong") int soLuong,
            @RequestParam("donGia") float donGia,
            @RequestParam("moTa") String moTa,
            @RequestParam("trangThai") String trangThai,
            @RequestParam(value = "anhSanPham", required = false) MultipartFile anhSanPham) {
        try {
            SanPhamChiTiet ctsp = new SanPhamChiTiet();
            SanPham sanPham = sanPhamRepository.findById(idsanPham)
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

            MauSac mauSac = mauSacRepository.findById(idmauSac)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy màu sắc với ID: " + idmauSac));
            KichThuoc size = kichThuocRepository.findById(idkichThuoc)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kích thước với ID: " + idkichThuoc));
            ChatLieu chatLieu = chatLieuRepository.findById(idchatLieu)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chất liệu với ID: " + idchatLieu));

            ctsp.setSanPham(sanPham);
            ctsp.setMauSac(mauSac);
            ctsp.setKichThuoc(size);
            ctsp.setChatLieu(chatLieu);
            ctsp.setSoLuong(soLuong);
            ctsp.setDonGia(donGia);
            ctsp.setMoTa(moTa);
            ctsp.setTrangThai(trangThai);

            // Xử lý lưu ảnh
            // Xử lý lưu ảnh
            if (anhSanPham != null && !anhSanPham.isEmpty()) {
                String uploadDir = "D:\\"; // Thư mục trong dự án
                String fileName = System.currentTimeMillis() + "_" + anhSanPham.getOriginalFilename();
                Path uploadPath = Paths.get(uploadDir);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath); // Tạo thư mục nếu chưa có
                }

                Path filePath = uploadPath.resolve(fileName);
                Files.copy(anhSanPham.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                ctsp.setAnhSanPham("/uploads/" + fileName); // Đường dẫn ảnh để hiển thị
            } else {
                ctsp.setAnhSanPham(null);
            }



            sanPhamCTRepository.save(ctsp);
            return "san_pham/ctsp";
        } catch (Exception e) {
            e.printStackTrace();
            return "san_pham/ctsp?error=true";
        }
    }

    // Cập nhật sản phẩm
    @PutMapping("/cap-nhat-ctsp/{id}")
    public ResponseEntity<?> updateCTSP(@PathVariable Integer id, @RequestBody Map<String, Object> payload) {
        // Tìm sản phẩm cần cập nhật dựa trên ID
        SanPhamChiTiet ctsp = sanPhamCTRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        // Cập nhật các trường của sản phẩm với dữ liệu từ payload
        ctsp.setSoLuong(Integer.parseInt((String)payload.get("soLuong")));
        ctsp.setDonGia(Float.parseFloat((String)payload.get("donGia")));
        ctsp.setAnhSanPham((String) payload.get("anhSanPham"));
        ctsp.setMoTa((String) payload.get("moTa"));
        ctsp.setTrangThai((String) payload.get("trangThai"));

        Integer idmauSac = Integer.parseInt((String) payload.get("mauSac"));
        MauSac mauSac = mauSacRepository.findById(idmauSac)
                .orElseThrow(() -> new RuntimeException("Màu sắc không tồn tại"));
        ctsp.setMauSac(mauSac);

        Integer idkichThuoc = Integer.parseInt((String) payload.get("kichThuoc"));
        KichThuoc size = kichThuocRepository.findById(idkichThuoc)
                .orElseThrow(() -> new RuntimeException("Kích thước không tồn tại"));
        ctsp.setKichThuoc(size);

        Integer idchatLieu = Integer.parseInt((String) payload.get("chatLieu"));
        ChatLieu chatLieu = chatLieuRepository.findById(idchatLieu)
                .orElseThrow(() -> new RuntimeException("Chất liệu không tồn tại"));
        ctsp.setChatLieu(chatLieu);



        // Lưu sản phẩm đã cập nhật vào cơ sở dữ liệu
        sanPhamCTRepository.save(ctsp);

        return ResponseEntity.ok("Sản phẩm đã được cập nhật thành công!");
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
