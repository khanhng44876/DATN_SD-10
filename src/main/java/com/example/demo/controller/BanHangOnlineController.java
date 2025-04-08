package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.exception.MessageException;
import com.example.demo.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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
    @Autowired
    private NhanVienRepository nhanVienRepository;
    @Autowired
    private KhuyenMaiRepository khuyenMaiRepository;
    @Autowired
    private SimpMessagingTemplate simMessagingTemplate;

    @RequestMapping("/ban-hang-online/admin")
        public String hienThi(Model model) {
            List<HoaDon> listhd = hoaDonrepo.findAll().stream()
                    .filter(hd->hd.getTrangThaiThanhToan().equals("Chờ xác nhận"))
                    .toList();
            model.addAttribute("listhd", listhd);
            return "ban_hang_online/adminHome.html";
        }

    @RequestMapping("/ban-hang-online/lshd/{id}")
    public String showLshd(Model model,@PathVariable int id) {
        HoaDon hoaDon = hoaDonrepo.findById(id).get();
        System.out.println(hoaDon);
        model.addAttribute("hdNCommit", hoaDon);
        List<HoaDonCT> listhdct = hdctRepository.findByHoaDonId(id);
        model.addAttribute("listhdct", listhdct);
        System.out.println(hoaDon);
        System.out.println(listhdct);
        return "ban_hang_online/lshd.html";
    }

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
        return "ban_hang_online/index";
    }

    @RequestMapping("/ban-hang-online/cart")
    public String idd(Model model){
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        KhachHang khachHang = khachHangRepository.findById(userDetails.getId()).orElse(null);
        model.addAttribute("kh",khachHang);
        return "ban_hang_online/giohang.html";
    }
    @RequestMapping("/ban-hang-online/dsdh-customer")
    public String iddsdh(Model model){
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        KhachHang khachHang = khachHangRepository.findById(userDetails.getId()).orElse(null);
        List<HoaDon> listhd = hoaDonrepo.findAll().stream()
                        .filter(hd -> hd.getKhachHang().getId() == khachHang.getId())
                                .toList();
        List<Map<String, Object>> listHoaDonData = new ArrayList<>();
        for (HoaDon hd : listhd) {
            Map<String, Object> obj = new HashMap<>();
            obj.put("hoaDon", hd);
            obj.put("chiTiet", hdctRepository.findByHoaDonId(hd.getId()));
            listHoaDonData.add(obj);
        }

        model.addAttribute("listhd", listHoaDonData);
        model.addAttribute("kh", khachHang);
        return "ban_hang_online/dsdh-customer.html";
    }

    @PostMapping("/ban-hang-online/create-order")
    @Transactional
    public ResponseEntity<?> createOrder(@RequestBody HoaDonRequestNew hd) {
        System.out.println("Dữ liệu nhận được: " + hd);
        HoaDon hoaDon = new HoaDon();
        hoaDon.setNgayTao(hd.getNgayTao());
        hoaDon.setNgaySua(hd.getNgaySua());
        hoaDon.setDonGia(hd.getDonGia());
        hoaDon.setTongTien(hd.getTongTien());
        hoaDon.setTrangThaiThanhToan(hd.getTrangThaiThanhToan());
        hoaDon.setHinhThucThanhToan(hd.getHinhThucThanhToan());
        hoaDon.setDiaChiGiaoHang(hd.getDiaChiGiaoHang());
        hoaDon.setGhiChu(hd.getGhiChu());

        hoaDon.setKhachHang(khachHangRepository.findById(hd.getIdKhachHang()).get());
        hoaDon.setNhanVien(nhanVienRepository.findById(hd.getIdNhanVien()).get());
        if(hd.getIdKhuyenMai() == null) {
            hoaDon.setKhuyenMai(null);
        }else {
            hoaDon.setKhuyenMai(khuyenMaiRepository.findById(hd.getIdKhuyenMai()).get());
        }

        hoaDonrepo.save(hoaDon);

        return ResponseEntity.ok(hoaDon);
    }

    @PostMapping("/ban-hang-online/create-order-ct")
    @Transactional
    public ResponseEntity<?> createOrderCT(@RequestBody HoaDonCT_DTO hdct){
        System.out.println(hdct);
        HoaDonCT hoaDonCT = new HoaDonCT();
        hoaDonCT.setDonGia(hdct.getDonGia());
        hoaDonCT.setSoLuong(hdct.getSoLuong());
        hoaDonCT.setTrangThai(hdct.getTrangThai());
        hoaDonCT.setTongTien(hdct.getTongTien());
        hoaDonCT.setThanhTien(hdct.getThanhTien());
        hoaDonCT.setNgayTao(hdct.getNgayTao());
        hoaDonCT.setNgaySua(hdct.getNgaySua());

        hoaDonCT.setHoaDon_id(hdct.getIdHoaDon());
        hoaDonCT.setCtsp_id(hdct.getIdSanPhamChiTiet());

        hdctRepository.save(hoaDonCT);
        return ResponseEntity.ok(hoaDonCT);
    }

    @PutMapping("/ban-hang-online/updateHD/{id}/{status}")
    public ResponseEntity<HoaDon> updateHD(@PathVariable Integer id,@PathVariable String status) {
        HoaDon hd = hoaDonrepo.findById(id).get();
        if(status.equals("Chờ xác nhận")){
            hd.setTrangThaiThanhToan("Chờ giao hàng");
        }
        if(status.equals("Chờ giao hàng")){
            hd.setTrangThaiThanhToan("Đang giao hàng");
        }
        if(status.equals("Đang giao hàng")){
            hd.setTrangThaiThanhToan("Giao hàng thành công");
        }
        if(status.equals("Giao hàng thành công")){
            hd.setTrangThaiThanhToan("Hoàn thành");
        }
        String newStatus = hd.getTrangThaiThanhToan();
        hoaDonrepo.save(hd);
        simMessagingTemplate.convertAndSend("/topic/order"+id, newStatus);
        return ResponseEntity.ok(hd);
    }

    @PutMapping("/ban-hang-online/updateCT/{id}/{status}")
    public ResponseEntity<HoaDonCT> updateCT(@PathVariable Integer id,@PathVariable String status) {
        HoaDonCT ct = hdctRepository.findById(id).get();
        if(status.equals("Chờ xác nhận")){
            ct.setTrangThai("Chờ giao hàng");
        }
        if(status.equals("Chờ giao hàng")){
            ct.setTrangThai("Đang giao hàng");
        }
        if(status.equals("Đang giao hàng")){
            ct.setTrangThai("Giao hàng thành công");
        }
        if(status.equals("Giao hàng thành công")){
            ct.setTrangThai("Hoàn thành");
        }
        hdctRepository.save(ct);
        return ResponseEntity.ok(ct);
    }

    @PutMapping("/ban-hang-online/update-sp/{id}/{so_luong}")
    public ResponseEntity<SanPhamChiTiet> updateSP(@PathVariable Integer id,@PathVariable Integer so_luong) {
        SanPhamChiTiet ct = ctsp_repository.findById(id).get();
        ct.setSoLuong(ct.getSoLuong()-so_luong);
        ctsp_repository.save(ct);
        return ResponseEntity.ok(ct);
    }

}
