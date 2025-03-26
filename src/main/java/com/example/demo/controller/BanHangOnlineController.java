package com.example.demo.controller;

import com.example.demo.dto.HoaDonRequest;
import com.example.demo.dto.SpctDto;
import com.example.demo.entity.*;
import com.example.demo.exception.MessageException;
import com.example.demo.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Controller

public class BanHangOnlineController {
    @Autowired
    private SanPhamRepository spRepository;
    @Autowired
    private SanPhamCTRepository ctsp_repository;
    @Autowired
    private KhachHangRepository khachHangRepository;
    @Autowired
    private HoaDonRepossitory hoaDonrepo;
    @Autowired
    private HoaDonCTRepository hdctRepository;
    @Autowired
    private DanhMucRepository danhMucRepository;


    @RequestMapping("/ban-hang-online")
    public String iddd(Model model) {
        List<SanPhamChiTiet> spClb = ctsp_repository.findAll().stream()
                .filter(sp->sp.getSanPham().getDanhMuc().getId() == 1)
                .filter(sp->sp.getSanPham().getTrangThai().equals("Còn hàng"))
                .toList();
        List<SanPhamChiTiet> spDtqg = ctsp_repository.findAll().stream()
                .filter(sp->sp.getSanPham().getDanhMuc().getId() == 2)
                .filter(sp->sp.getSanPham().getTrangThai().equals("Còn hàng"))
                .limit(5)
                .toList();
        List<SanPhamChiTiet> spNologo = ctsp_repository.findAll().stream()
                .filter(sp->sp.getSanPham().getDanhMuc().getId() == 3)
                .filter(sp->sp.getSanPham().getTrangThai().equals("Còn hàng"))
                .limit(5)
                .toList();
        model.addAttribute("spDtqg",spDtqg);
        model.addAttribute("spNologo",spNologo);
        model.addAttribute("spClb", spClb);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Kiểm tra nếu user chưa đăng nhập (anonymous)
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            return "ban_hang_online/home";  // Trang chưa đăng nhập
        }

        return "ban_hang_online/index";  // Trang đã đăng nhập
    }

    @RequestMapping("/ban-hang-online/cart")
    public String idd(){

        return "ban_hang_online/giohang";
    }
    @RequestMapping("/ban-hang-online/sp")
    public String iddsp(Model model){
        model.addAttribute("listSp", ctsp_repository.findAll());
        return "ban_hang_online/product.html";
    }


    @PostMapping("/ban-hang-online/hoa-don")
    @ResponseBody
    public ResponseEntity<?> createHoaDon(@RequestBody HoaDonRequest request, HttpSession session) {
        try {
            NhanVien taiKhoan = (NhanVien) session.getAttribute("nhanVien");

            if (taiKhoan == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chưa đăng nhập!");
            }

            Float tongTien = 0F;
            for (SpctDto d : request.getSpct()) {
                Optional<SanPhamChiTiet> ctspOptional = ctsp_repository.findById(d.getIdCtsp());

                if (!ctspOptional.isPresent()) {
                    return ResponseEntity.badRequest().body("Không tìm thấy sản phẩm chi tiết ID: " + d.getIdCtsp());
                }

                SanPhamChiTiet ctsp = ctspOptional.get();
                if (ctsp.getSoLuong() < d.getQuantity()) {
                    return ResponseEntity.badRequest().body("Sản phẩm [" + ctsp.getSanPham().getTenSanPham() + "] chỉ còn: " + ctsp.getSoLuong());
                }
                tongTien += ctsp.getDonGia() * d.getQuantity();
            }

            KhachHang khachHang = new KhachHang();
            khachHang.setTenKhachHang(request.getTenKhachHang() != null ? request.getTenKhachHang() : "Khách hàng không rõ");
            khachHang.setSoDienThoai(request.getSoDienThoai());
            khachHang.setDiaChi(request.getDiaChi());
            khachHang = khachHangRepository.save(khachHang);

            HoaDon hoaDon = new HoaDon();
            hoaDon.setNgayTao(new Date(System.currentTimeMillis()));
            hoaDon.setNgaySua(new Date(System.currentTimeMillis()));
            hoaDon.setTongTien(tongTien);
            hoaDon.setHinhThucThanhToan(request.getHinhThucThanhToan());
            hoaDon.setTrangThaiThanhToan("Cho giao hang");
            hoaDon.setKhachHang(khachHang);
            hoaDon.setNhanVien(taiKhoan);
            hoaDon = hoaDonrepo.save(hoaDon);

            for (SpctDto d : request.getSpct()) {
                SanPhamChiTiet ctsp = ctsp_repository.findById(d.getIdCtsp()).get();

                HoaDonCT hdct = new HoaDonCT();
                hdct.setHoaDon(hoaDon);
                hdct.setDonGia(ctsp.getDonGia());
                hdct.setHoaDon_id(hoaDon.getId());
                hdct.setCtsp_id(ctsp.getId());
                hdct.setSanPhamChiTiet(ctsp);
                hdct.setTongTien(tongTien);
                hdct.setSoLuong(d.getQuantity());

                // Đảm bảo tính toán và set giá trị thanhTien
                hdct.setThanhTien(ctsp.getDonGia() * d.getQuantity());

                hdctRepository.save(hdct);

                // Cập nhật số lượng tồn kho
                ctsp.setSoLuong(ctsp.getSoLuong() - d.getQuantity());
                ctsp_repository.save(ctsp);
            }


            return new ResponseEntity<>(hoaDon, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi đặt hàng: " + e.getMessage());
        }
    }





    @GetMapping("ban-hang-online/don-hang")
    public String xac(Model model) {
        // Danh sách trạng thái cần lọc
        List<String> trangThaiList = Arrays.asList("Cho giao hang", "Dang giao hang");

        // Lấy danh sách hóa đơn theo trạng thái
        List<HoaDon> ds = this.hoaDonrepo.findByTrangThaiThanhToanIn(trangThaiList);

        // Thêm dữ liệu vào model để hiển thị trong view
        model.addAttribute("donHang", ds);

        return "ban_hang_online/donhang";
    }
    //    @GetMapping("ban-hang-online/da-thanh-toan")
//    public String dttt(Model model){
//        List<HoaDon> dtt = this.hoaDonrepo.findByTrangThaiThanhToan("Giao hang thanh cong");
//        model.addAttribute("dtt", dtt);
//        return "bhonline/datt";
//    }
    @GetMapping("ban-hang-online/da-thanh-toan")
    public String viewListDonHangdtt(Model model, @RequestParam(required = false) String trangThai) {
        System.out.println("TrangThai nhận được: " + trangThai); // Debug giá trị
        List<HoaDon> hoaDonList;

        if ("Giao hang thanh cong".equalsIgnoreCase(trangThai != null ? trangThai.trim() : "")) {
            hoaDonList = hoaDonrepo.findByTrangThaiThanhToanOrderByIdDesc("Giao hang thanh cong");
        } else {
            hoaDonList = hoaDonrepo.findAllByOrderByIdDesc();
        }

        // Xác minh danh sách trả về chỉ chứa trạng thái mong muốn
        hoaDonList = hoaDonList.stream()
                .filter(hd -> "Giao hang thanh cong".equalsIgnoreCase(hd.getTrangThaiThanhToan()))
                .collect(Collectors.toList());

        model.addAttribute("dtt", hoaDonList);
        return "ban_hang_online/datt";
    }


    //    @GetMapping("ban-hang-online/da-huy")
//    public String dhhhh(Model model){
//        List<HoaDon> dtt = this.hoaDonrepo.findByTrangThaiThanhToan("Da huy");
//        model.addAttribute("dhh", dtt);
//        return "bhonline/dhh";
//    }
    @GetMapping("ban-hang-online/da-huy")
    public String viewListDonHangdh(Model model, @RequestParam(required = false) String trangThai) {
        System.out.println("TrangThai nhận được: " + trangThai); // Debug giá trị
        List<HoaDon> hoaDonList;

        if ("Da huy".equalsIgnoreCase(trangThai != null ? trangThai.trim() : "")) {
            hoaDonList = hoaDonrepo.findByTrangThaiThanhToanOrderByIdDesc("Da huy");
        } else {
            hoaDonList = hoaDonrepo.findAllByOrderByIdDesc();
        }

        // Xác minh danh sách trả về chỉ chứa trạng thái mong muốn
        hoaDonList = hoaDonList.stream()
                .filter(hd -> "Da huy".equalsIgnoreCase(hd.getTrangThaiThanhToan()))
                .collect(Collectors.toList());

        model.addAttribute("dhh", hoaDonList);
        return "ban_hang_online/dhh";
    }







    @PutMapping("/ban-hang-online/hoa-don/{id}")
    @ResponseBody
    public ResponseEntity<?> updateTrangThaiHoaDon(
            @PathVariable("id") Integer id,
            @RequestBody Map<String, String> request) {

        try {
            // Lấy trạng thái từ request
            String trangThaiMoi = request.get("trangThaiThanhToan");
            if (trangThaiMoi == null || trangThaiMoi.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Trạng thái thanh toán không được để trống!");
            }

            // Tìm hóa đơn theo ID
            Optional<HoaDon> optionalHoaDon = hoaDonrepo.findById(id);
            if (optionalHoaDon.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Hóa đơn không tồn tại!");
            }

            // Cập nhật trạng thái thanh toán
            HoaDon hoaDon = optionalHoaDon.get();
            hoaDon.setTrangThaiThanhToan(trangThaiMoi);

            // Lưu lại thay đổi
            HoaDon updatedHoaDon = hoaDonrepo.save(hoaDon);

            return new ResponseEntity<>(updatedHoaDon, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/ban-hang-online/detail")
    public String detailPage(@RequestParam("id") Integer id, Model model) {
        Optional<SanPhamChiTiet> optionalCtsp = ctsp_repository.findById(id);
        if (optionalCtsp.isPresent()) {
            model.addAttribute("product", optionalCtsp.get()); // Truyền dữ liệu nếu cần dùng Thymeleaf
            return "ban_hang_online/detail"; // Trả về file detail.html
        } else {
            return "ban_hang_online/error"; // Trang lỗi nếu không tìm thấy sản phẩm
        }
    }

    @GetMapping("/ban-hang-online/danh-muc")
    @ResponseBody
    public ResponseEntity<?> getAllDanhMuc() {
        try {
            List<DanhMuc> danhMucList = danhMucRepository.findAll();
            if (danhMucList.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy danh mục nào.");
            }
            return new ResponseEntity<>(danhMucList, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi lấy danh sách danh mục: " + e.getMessage());
        }
    }
    // Thêm phương thức mới để lấy chi tiết sản phẩm theo ID
    @GetMapping("/ban-hang-online/ctsp/{id}")
    @ResponseBody
    public ResponseEntity<?> getProductDetail(@PathVariable("id") Integer id) {
        try {
            Optional<SanPhamChiTiet> optionalCtsp = ctsp_repository.findById(id);
            if (!optionalCtsp.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy sản phẩm với ID: " + id);
            }
            SanPhamChiTiet ctsp = optionalCtsp.get();
            return new ResponseEntity<>(ctsp, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi lấy chi tiết sản phẩm: " + e.getMessage());
        }
    }
}
