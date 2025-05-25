package com.example.demo.controller;

import com.example.demo.entity.SanPhamChiTiet;
import com.example.demo.repository.ChatLieuRepository;
import com.example.demo.repository.KichThuocRepository;
import com.example.demo.repository.MauSacRepository;
import com.example.demo.repository.SanPhamCTRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@Slf4j
public class ChiTietSPOnlineController {

    private static final String STATUS_AVAILABLE = "Còn hàng";

    @Autowired
    private SanPhamCTRepository sanPhamCTRepository;
    @Autowired
    private KichThuocRepository kichThuocRepository;
    @Autowired
    private ChatLieuRepository chatLieuRepository;
    @Autowired
    private MauSacRepository mauSacRepository;

    @GetMapping("/ban-hang-online/detail")
    @PreAuthorize("permitAll()")
    public String detailPage(@RequestParam("id") Integer id, Model model) {
        try {
            Optional<SanPhamChiTiet> optionalCtsp = sanPhamCTRepository.findById(id);
            if (!optionalCtsp.isPresent()) {
                log.warn("Không tìm thấy sản phẩm chi tiết với ID: {}", id);
                return "ban_hang_online/error";
            }

            SanPhamChiTiet ctsp = optionalCtsp.get();
            log.info("Đã tìm thấy sản phẩm chi tiết ID: {}", id);

            List<SanPhamChiTiet> allDetails = sanPhamCTRepository.findBySanPhamId(ctsp.getSanPham().getId());
            if (allDetails.isEmpty()) {
                log.warn("Không có chi tiết nào cho sản phẩm ID: {}", ctsp.getSanPham().getId());
                model.addAttribute("error", "Sản phẩm không có biến thể hoặc kích thước nào.");
                return "ban_hang_online/error";
            }
            System.out.println(allDetails);
            List<SanPhamChiTiet> uniqueVariants = getUniqueDetails(allDetails,
                    detail -> detail.getMauSac().getTen_mau_sac() + "-" + detail.getChatLieu().getTenChatLieu());

            // Tạo ánh xạ biến thể với kích thước, bao gồm giá
            Map<String, List<Map<String, Object>>> variantToSizesMap = allDetails.stream()
                    .filter(detail -> detail.getSoLuong() > 0 && STATUS_AVAILABLE.equals(detail.getTrangThai()))
                    .collect(Collectors.groupingBy(
                            detail -> detail.getMauSac().getTen_mau_sac() + "-" + detail.getChatLieu().getTenChatLieu(),
                            Collectors.mapping(detail -> {
                                Map<String, Object> sizeData = new HashMap<>();
                                sizeData.put("id", detail.getId());
                                sizeData.put("tenKichThuoc", detail.getKichThuoc().getTenKichThuoc());
                                sizeData.put("soLuong", detail.getSoLuong());
                                sizeData.put("donGia", detail.getDonGia()); // Thêm giá
                                return sizeData;
                            }, Collectors.toList())
                    ));
            System.out.println(variantToSizesMap);
            model.addAttribute("product", ctsp);
            model.addAttribute("variants", uniqueVariants);
            model.addAttribute("variantToSizesMap", variantToSizesMap);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isAnonymous = isUserAnonymous(authentication);
            model.addAttribute("isAuthenticated", !isAnonymous);

            log.info("variantToSizesMap: {}", variantToSizesMap); // Debug dữ liệu
            return "ban_hang_online/ctsp";

        } catch (Exception e) {
            log.error("Lỗi khi tải trang chi tiết sản phẩm ID: {}. Chi tiết: {}", id, e.getMessage());
            model.addAttribute("error", "Đã xảy ra lỗi khi tải trang chi tiết sản phẩm.");
            return "ban_hang_online/error";
        }
    }

    private List<SanPhamChiTiet> getUniqueDetails(List<SanPhamChiTiet> allDetails,
                                                  Function<SanPhamChiTiet, String> keyExtractor) {
        return allDetails.stream()
                .filter(detail -> detail.getSoLuong() > 0 && STATUS_AVAILABLE.equals(detail.getTrangThai()))
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                keyExtractor,
                                detail -> detail,
                                (existing, replacement) -> existing,
                                LinkedHashMap::new
                        ),
                        map -> new ArrayList<>(map.values())
                ));
    }

    private boolean isUserAnonymous(Authentication authentication) {
        return authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal());
    }
    @RequestMapping("/ban-hang-online/sp")
    public String iddsp(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size, // 9 sản phẩm mỗi trang
            Model model) {
        Pageable pageable = (Pageable) PageRequest.of(page, size);
        Page<SanPhamChiTiet> productPage = sanPhamCTRepository.findAll(pageable);

        model.addAttribute("listSp", productPage.getContent());
        model.addAttribute("currentPage", productPage.getNumber());
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("sizes", kichThuocRepository.findAll());
        model.addAttribute("colors", mauSacRepository.findAll());
        model.addAttribute("materials", chatLieuRepository.findAll());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return "ban_hang_online/product";

    }

    @GetMapping("/ban-hang-online/sp/filter")
    public String filterProducts(
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) List<String> sizes,
            @RequestParam(required = false) List<String> colors,
            @RequestParam(required = false) List<String> materials,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<SanPhamChiTiet> productPage = sanPhamCTRepository.filterProducts(minPrice, maxPrice, sizes, colors, materials, pageable);

        model.addAttribute("listSp", productPage.getContent());
        model.addAttribute("currentPage", productPage.getNumber());
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("sizes", kichThuocRepository.findAll());
        model.addAttribute("colors", mauSacRepository.findAll());
        model.addAttribute("materials", chatLieuRepository.findAll());
        return "ban_hang_online/product";

    }
}