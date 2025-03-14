package com.example.demo.controller;

import com.example.demo.dto.HoaDonRequest;
import com.example.demo.dto.SpctDto;
import com.example.demo.entity.*;
import com.example.demo.exception.MessageException;
import com.example.demo.repository.SanPhamCTRepository;
import com.example.demo.repository.HoaDonCTRepository;
import com.example.demo.repository.HoaDonRepossitory;
import com.example.demo.repository.KhachHangRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller

public class BanHangOnlineController {
    @Autowired
    private SanPhamCTRepository ctsp_repository;
    @Autowired
    private KhachHangRepository khachHangRepository;
    @Autowired
    private HoaDonRepossitory hoaDonrepo;
    @Autowired
    private HoaDonCTRepository hdctRepository;

    @RequestMapping("/ban-hang-online")
    public String iddd(){

        return "ban_hang_online/index";
    }
    @RequestMapping("/ban-hang-online/cart")
    public String idd(){

        return "ban_hang_online/giohang";
    }


    @GetMapping("/ban-hang-online/ctsp")
    @ResponseBody
    public ResponseEntity<?> index1() {
        List<SanPhamChiTiet> result = this.ctsp_repository.findAll();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/ban-hang-online/hoa-don")
    @ResponseBody
    public ResponseEntity<?> createHoaDon(@RequestBody HoaDonRequest request, HttpSession session) {
        try {
            NhanVien taiKhoan = (NhanVien) session.getAttribute("nhanVien");

            if (taiKhoan == null) {
                System.out.println("Lỗi: Không tìm thấy thông tin tài khoản trong session!");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Không tìm thấy thông tin tài khoản!");
            }


            Float tongTien = 0F;
            for (SpctDto d : request.getSpct()) {
                SanPhamChiTiet ctsp = ctsp_repository.findById(d.getIdCtsp()).get();
                if (ctsp.getSoLuong() < d.getQuantity()) {
                    throw new MessageException("id CTSP " + ctsp.getId() + " chỉ còn " + ctsp.getSoLuong());
                }
                tongTien += ctsp.getDonGia() * d.getQuantity();
            }

            // Tạo đối tượng khách hàng
            KhachHang khachHang = new KhachHang();
            khachHang.setTenKhachHang(request.getTenKhachHang() != null ? request.getTenKhachHang() : "Khách hàng không rõ");
            khachHang.setSoDienThoai(request.getSdt());
            khachHang.setDiaChi(request.getDiaChi());

            // Lưu khách hàng
            khachHang = khachHangRepository.save(khachHang);

            // Tạo đối tượng hóa đơn
            HoaDon hoaDon = new HoaDon();
            hoaDon.setNgayTao((java.sql.Date) new Date(System.currentTimeMillis()));
            hoaDon.setTongTien(request.getTongTien());
            hoaDon.setHinhThucThanhToan(request.getHinhThucThanhToan());
            hoaDon.setTrangThaiThanhToan("Cho giao hang");
            hoaDon.setKhachHang(khachHang);
            hoaDon.setNhanVien(taiKhoan);

            // Lưu hóa đơn
            HoaDon savedHoaDon = hoaDonrepo.save(hoaDon);

            // Cập nhật chi tiết hóa đơn
            for (SpctDto d : request.getSpct()) {
                Optional<SanPhamChiTiet> ctspOptional = ctsp_repository.findById(d.getIdCtsp());
                if (ctspOptional.isPresent()) {
                    SanPhamChiTiet ctsp = ctspOptional.get();
                    HoaDonCT hdct = new HoaDonCT();
                    hdct.setHoaDon(hoaDon);
                    hdct.setDonGia(ctsp.getDonGia());
                    hdct.setHoaDon_id(hoaDon.getId());
                    hdct.setCtsp_id(ctsp.getId());
                    hdct.setSanPhamChiTiet(ctsp);
                    hdct.setSoLuong(d.getQuantity());
                    hdct.setThanhTien(d.getQuantity() * ctsp.getDonGia());
                    hdctRepository.save(hdct);
                } else {
                    // Log hoặc xử lý khi không tìm thấy sản phẩm
                    System.out.println("Không tìm thấy sản phẩm với ID: " + d.getIdCtsp());
                }
            }


            // Cập nhật số lượng sản phẩm
            for (SpctDto d : request.getSpct()) {
                SanPhamChiTiet ctsp = ctsp_repository.findById(d.getIdCtsp()).get();
                ctsp.setSoLuong(ctsp.getSoLuong() - d.getQuantity());
                ctsp_repository.save(ctsp);
            }

            return new ResponseEntity<>(savedHoaDon, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
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


}
