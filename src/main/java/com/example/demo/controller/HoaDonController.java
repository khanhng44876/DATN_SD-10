package com.example.demo.controller;

import com.example.demo.entity.HoaDon;
import com.example.demo.entity.SanPham;
import com.example.demo.repository.HoaDonRepossitory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("hoa-don")
public class HoaDonController {
    @Autowired
    HoaDonRepossitory hoaDonRepo;

    @GetMapping("/hien-thi")
    public String viewListDonHang(Model model, @RequestParam(required = false) String trangThai) {
        System.out.println("TrangThai nhận được: " + trangThai);
        List<HoaDon> hoaDonList = hoaDonRepo.findByTrangThai(
                (trangThai != null && !trangThai.trim().isEmpty()) ? trangThai.trim() : null
        );

        model.addAttribute("listHoaDon", hoaDonList);
        return "/hoa_don/qlhd";
    }

}
