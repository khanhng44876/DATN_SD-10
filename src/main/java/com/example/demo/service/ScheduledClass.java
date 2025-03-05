package com.example.demo.service;

import com.example.demo.entity.KhuyenMai;
import com.example.demo.repository.KhuyenMaiRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ScheduledClass {
    private KhuyenMaiRepository repo;
    public ScheduledClass(KhuyenMaiRepository repo) {
        this.repo = repo;
    }
    @PostConstruct
    public void init() {
        updateTrangThai();
    }
    @Scheduled(cron = "0 0 0 * * ?")
    public void updateTrangThai(){
        LocalDate today = LocalDate.now();
        List<KhuyenMai> list = repo.findAll();
        for(KhuyenMai km : list){
            if (today.isBefore(km.getNgay_bat_dau())) {
                km.setTrang_thai("Sắp diễn ra");
            } else if (!today.isBefore(km.getNgay_bat_dau()) && !today.isAfter(km.getNgay_ket_thuc())) {
                km.setTrang_thai("Đang diễn ra");
            } else {
                km.setTrang_thai("Đã kết thúc");
            }
        }
        repo.saveAll(list);
    }
}
