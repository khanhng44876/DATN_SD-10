package com.example.demo.controller;

import com.example.demo.entity.MauSac;
import com.example.demo.repository.MauSacRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/mau-sac")
public class MauSacController {
    @Autowired
    MauSacRepository repo;

    @GetMapping("/hien-thi")
    public String getList(Model model) {
        List<MauSac> msList = repo.findAll();
        model.addAttribute("msList", msList);
        return "mau_sac/index";
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<MauSac> getOne(@PathVariable int id) {
        if(!repo.existsById(id)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(repo.findById(id).get());
    }

    @PostMapping("/add")
    public ResponseEntity<MauSac> add(@RequestBody MauSac ms) {
        repo.save(ms);
        return ResponseEntity.ok(ms);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<MauSac> update(@PathVariable int id, @RequestBody MauSac ms) {
        if(!repo.existsById(id)){
            return ResponseEntity.notFound().build();
        }
        ms.setId(id);
        repo.save(ms);
        return ResponseEntity.ok(ms);
    }

}
