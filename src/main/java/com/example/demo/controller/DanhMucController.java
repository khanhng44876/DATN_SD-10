package com.example.demo.controller;
import com.example.demo.repository.DanhMucRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/DATN_SD10")
public class DanhMucController {
    @Autowired
    DanhMucRepository danhMucRepository;
    // Hien thi
    @GetMapping("/hien-thi-danh-muc")
    public String hienThi(Model model){
        model.addAttribute("listDanhMuc",danhMucRepository.findAll());
        return "hien-thi-danh-muc";
    }

}
