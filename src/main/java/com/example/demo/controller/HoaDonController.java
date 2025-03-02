package com.example.demo.controller;

import com.example.demo.entity.HoaDon;
import com.example.demo.entity.HoaDonCT;
import com.example.demo.entity.SanPham;
import com.example.demo.repository.HoaDonCTRepository;
import com.example.demo.repository.HoaDonRepossitory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("hoa-don")
public class HoaDonController {
    @Autowired
    HoaDonRepossitory hoaDonRepo;

    @Autowired
    HoaDonCTRepository hoaDonCTRepo;
    @GetMapping()
    public String viewListDonHang(Model model, @RequestParam(required = false) String trangThai) {
        System.out.println("TrangThai nhận được: " + trangThai); // Debug giá trị
        List<HoaDon> hoaDonList;

        if ("Da thanh toan".equalsIgnoreCase(trangThai != null ? trangThai.trim() : "")) {
            hoaDonList = hoaDonRepo.findByTrangThaiThanhToan("Da thanh toan");
        } else {
            hoaDonList = hoaDonRepo.findAllByOrderByIdDesc();
        }

        // Xác minh danh sách trả về chỉ chứa trạng thái mong muốn
//        hoaDonList = hoaDonList.stream()
//                .filter(hd -> "Da thanh toan".equalsIgnoreCase(hd.getTrangThaiThanhToan()))
//                .collect(Collectors.toList());

        model.addAttribute("listHoaDon", hoaDonList);
        return "/hoa_don/qlhd";
    }
    @GetMapping("/chi-tiet-hoa-don")
    @ResponseBody
    public ResponseEntity<?> chiTietDonHang(@RequestParam Integer hoaDonId){
        List<HoaDonCT> result = hoaDonCTRepo.findByHoaDonId(hoaDonId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

//    doanh thu=================================================
    @GetMapping("/doanh-thu")
    public String doanhThu(){
        return "/hoa_don/doanhThu";
    }
}
