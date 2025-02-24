package com.example.demo.controller;

import com.example.demo.entity.ChatLieu;
import com.example.demo.entity.DanhMuc;
import com.example.demo.entity.SanPham;
import com.example.demo.repository.ChatLieuRepository;
import com.example.demo.repository.DanhMucRepository;
import com.example.demo.repository.SanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("san-pham")
public class QuanLiSanPhamController {

    @Autowired
     private SanPhamRepository sanPhamRepository;
    @Autowired
    private DanhMucRepository danhMucRepository;
    @Autowired
    private ChatLieuRepository chatLieuRepository;

//
//    @ModelAttribute("dsChatLieu")
//    public List<ChatLieu> getcl(){
//        return chatLieuRepository.findAll();
//    }
//    @ModelAttribute("dsDanhMuc")
//    public List<DanhMuc> gettt(){return danhMucRepository.findAll();}


    @GetMapping("hien-thi")
    public String index(Model model){
        List<SanPham> ds = this.sanPhamRepository.findAll();
        // Kiểm tra xem danh mục có được gán đúng không
        ds.forEach(sp -> System.out.println(sp.getDanhMuc()));
        ds.forEach(sp -> System.out.println(sp.getHang()));
        model.addAttribute("dataSP", ds);
        return "san_pham/qlsp";
    }

    @GetMapping("chat-lieu")
    public String chatlieu(Model model){
        List<ChatLieu> ds = this.chatLieuRepository.findAll();
        model.addAttribute("listct",ds);
        return "san_pham/chatlieu";
    }

    @GetMapping("danh-muc")
    public String listddh(Model model){
        List<DanhMuc> ds = this.danhMucRepository.findAll();
        model.addAttribute("listdm", ds);
        return "san_pham/danhmuc";
    }
}
