package com.example.demo.controller;

import com.example.demo.entity.KhuyenMai;
import com.example.demo.repository.KhuyenMaiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/khuyen-mai")
public class KhuyenMaiController {
    @Autowired
    KhuyenMaiRepository repo;

    @GetMapping("/hien-thi")
    public String hienThi(Model model) {
        List<KhuyenMai> list = repo.findAll();
        System.out.println(list);
        model.addAttribute("listkm", list);
        return "khuyen_mai/qlkm";
    }

    @PostMapping("/add-km")
    public ResponseEntity<KhuyenMai> addKm(@RequestBody KhuyenMai km) {
        LocalDate today = LocalDate.now();
        if(today.isBefore(km.getNgay_bat_dau())){
            km.setTrang_thai("Sắp diễn ra");
        }else if(!today.isBefore(km.getNgay_bat_dau()) && !today.isAfter(km.getNgay_ket_thuc())){
            km.setTrang_thai("Đang diễn ra");
        }else {
            km.setTrang_thai("Đã kết thúc");
        }
        System.out.println(km.getNgay_ket_thuc());
        repo.save(km);
        return ResponseEntity.ok(km);
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<KhuyenMai> detail(@PathVariable Integer id) {
        if(!repo.existsById(id)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(repo.findById(id).get());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<KhuyenMai> update(@PathVariable Integer id, @RequestBody KhuyenMai km) {
        if(!repo.existsById(id)){
            return ResponseEntity.notFound().build();
        }
        LocalDate today = LocalDate.now();
        if(today.isBefore(km.getNgay_bat_dau())){
            km.setTrang_thai("Sắp diễn ra");
        }else if(!today.isBefore(km.getNgay_bat_dau()) && !today.isAfter(km.getNgay_ket_thuc())){
            km.setTrang_thai("Đang diễn ra");
        }else {
            km.setTrang_thai("Đã kết thúc");
        }
        km.setId(id);
        repo.save(km);
        return ResponseEntity.ok(km);
    }

}
