package com.example.demo.controller;

import com.example.demo.entity.SanPham;
import com.example.demo.repository.DanhMucRepository;
import com.example.demo.repository.SanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("san-pham")
public class QuanLiSanPhamController {

    @Autowired
     private SanPhamRepository sanPhamRepository;
    @Autowired
    private DanhMucRepository danhMucRepository;



    @GetMapping("hien-thi")
    public String index(Model model){
        List<SanPham> ds = this.sanPhamRepository.findAll();
        // Kiểm tra xem danh mục có được gán đúng không
        ds.forEach(sp -> System.out.println(sp.getDanhMuc()));
        ds.forEach(sp -> System.out.println(sp.getHang()));
        model.addAttribute("dataSP", ds);
        return "san_pham/index";
    }
}
