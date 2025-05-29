package com.example.demo.controller;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.service.VNPAYService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;

@Controller
public class VNPAYController {

    private static final Logger logger = LoggerFactory.getLogger(VNPAYController.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private VNPAYService vnPayService;

    @Autowired
    private HoaDonRepossitory hoaDonRepository;
    @Autowired
    private ThongBaoRepository thongBaoRepository;
    @Autowired
    private HoaDonCTRepository hoaDonCTRepository;
    @Autowired
    private SanPhamCTRepository sanPhamCTRepository;
    @Autowired
    private KhuyenMaiRepository khuyenMaiRepository;

@GetMapping("/ban-hang-off/payment-return")
    public String offlineReturn(HttpServletRequest request, Model model) {
        String vnp_TxnRef       = request.getParameter("vnp_TxnRef");
        String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
        String vnp_SecureHash   = request.getParameter("vnp_SecureHash");

        Map<String, String[]> raw = request.getParameterMap();
        Map<String, String> params = raw.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue() != null && e.getValue().length > 0 ? e.getValue()[0] : ""
                ));

        if (!vnPayService.verifySignature(params, vnp_SecureHash)) {
            model.addAttribute("status", "error");
            model.addAttribute("message", "Chữ ký không hợp lệ!");
            return "ban_hang_off/paymentResult";
        }

        String status = "fail";
        if ("00".equals(vnp_ResponseCode)) {
            status = "success";
            model.addAttribute("status", "success");
            model.addAttribute("message", "Thanh toán thành công!");
            model.addAttribute("txnRef", vnp_TxnRef);
        } else {
            model.addAttribute("status", "fail");
            model.addAttribute("message", "Thanh toán thất bại, mã lỗi: " + vnp_ResponseCode);
        }
        return "redirect:/ban-hang-off/hien-thi?paymentResult=" + status;
    }

    @GetMapping("/vnpay-payment-return")
    @Transactional
    public String paymentCompleted(HttpServletRequest request, Model model) {
        String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
        String orderInfo        = request.getParameter("vnp_OrderInfo");
        String paymentTime      = request.getParameter("vnp_PayDate");
        String transactionId    = request.getParameter("vnp_TransactionNo");
        String totalPrice       = request.getParameter("vnp_Amount");

        String orderId = extractOrderId(orderInfo);
        if (orderId == null) {
            logger.error("Không thể trích xuất orderId từ orderInfo: {}", orderInfo);
            model.addAttribute("error", "Lỗi xử lý đơn hàng: Không tìm thấy ID hóa đơn");
            return "ban_hang_online/paymentResult";
        }

        if ("00".equals(vnp_ResponseCode)) {
            processSuccessfulPayment(orderId);
            model.addAttribute("paymentStatus", "Thành công");
        } else {
            processFailedOrCancelled(orderId, vnp_ResponseCode);
            model.addAttribute("paymentStatus", getFailureStatus(vnp_ResponseCode));
        }

        model.addAttribute("orderId", orderInfo);
        model.addAttribute("totalPrice", totalPrice != null ? Long.parseLong(totalPrice) / 100 : 0);
        model.addAttribute("paymentTime", paymentTime);
        model.addAttribute("transactionId", transactionId);

        return "ban_hang_online/paymentResult";
    }

    @PostMapping("/ban-hang-online/vnpay-ipn")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Map<String, String>> handleIPN(HttpServletRequest request) {
        Map<String, String> fields = new TreeMap<>();
        for (String key : request.getParameterMap().keySet()) {
            String value = request.getParameter(key);
            if (value != null && !value.isEmpty()) {
                fields.put(key, value);
            }
        }

        String vnp_SecureHash = fields.get("vnp_SecureHash");
        Map<String, String> response = new HashMap<>();

        if (!vnPayService.verifySignature(fields, vnp_SecureHash)) {
            response.put("RspCode", "97");
            response.put("Message", "Invalid signature");
            return ResponseEntity.ok(response);
        }

        String vnp_ResponseCode = fields.get("vnp_ResponseCode");
        String orderInfo        = fields.get("vnp_OrderInfo");
        String orderId          = extractOrderId(orderInfo);

        if (orderId == null) {
            response.put("RspCode", "99");
            response.put("Message", "Invalid orderId");
            return ResponseEntity.ok(response);
        }

        if ("00".equals(vnp_ResponseCode)) {
            processSuccessfulPayment(orderId);
            response.put("RspCode", "00");
            response.put("Message", "Confirm Success");
        } else {
            processFailedOrCancelled(orderId, vnp_ResponseCode);
            response.put("RspCode", "99");
            response.put("Message", "Confirm Failed");
        }

        return ResponseEntity.ok(response);
    }

    private void processSuccessfulPayment(String orderId) {
        updateOrderStatus(orderId, "Chờ xác nhận");

        List<HoaDonCT> chiTiet = hoaDonCTRepository.findByHoaDonId(Integer.valueOf(orderId));
        for (HoaDonCT ct : chiTiet) {
            SanPhamChiTiet sp = ct.getSanPhamChiTiet();
            sp.setSoLuong(sp.getSoLuong() - ct.getSoLuong());
            if (sp.getSoLuong() == 0) {
                sp.setTrangThai("Hết hàng");
            }
            sanPhamCTRepository.save(sp);
        }

        hoaDonRepository.findById(Integer.parseInt(orderId)).ifPresent(hd -> {
            KhuyenMai km = hd.getKhuyenMai();
            if (km != null) {
                km.setSo_luong(km.getSo_luong() - 1);
                km.setSo_luong_sd(km.getSo_luong_sd() + 1);
                if (km.getSo_luong() == 0) {
                    km.setTrang_thai("Đã kết thúc");
                }
                khuyenMaiRepository.save(km);
            }
        });
    }

    private void processFailedOrCancelled(String orderId, String responseCode) {
        final String message = "24".equals(responseCode)
                ? "Khách hàng hủy giao dịch"
                : "Thanh toán thất bại";

        hoaDonRepository.findById(Integer.parseInt(orderId)).ifPresent(hd -> {
            ThongBao tb = new ThongBao();
            tb.setLink("/ban-hang-online/cart");
            tb.setNoi_dung(message);
            tb.setKhachHang(hd.getKhachHang());
            tb.setNgayTao(new Date());
            thongBaoRepository.save(tb);
            hoaDonRepository.delete(hd);
        });
    }

    private String extractOrderId(String orderInfo) {
        try {
            if (orderInfo == null || orderInfo.isEmpty()) return null;
            String[] parts = orderInfo.split(" ");
            if (parts.length < 5) return null;
            return parts[4];
        } catch (Exception e) {
            logger.error("Lỗi khi lấy orderId từ orderInfo: {}", orderInfo, e);
            return null;
        }
    }

    private void updateOrderStatus(String orderId, String status) {
        try {
            Optional<HoaDon> opt = hoaDonRepository.findById(Integer.parseInt(orderId));
            if (opt.isPresent()) {
                HoaDon hd = opt.get();
                hd.setNgaySua(new Date());
                hd.setTrangThaiThanhToan(status);
                HoaDon saved = hoaDonRepository.save(hd);

                ThongBao tb = new ThongBao();
                tb.setLink("/ban-hang-online/follow-order/" + saved.getId());
                tb.setNoi_dung("Đơn hàng HD" + saved.getId() + " của bạn đang trong trạng thái: " + status);
                tb.setNgayTao(new Date());
                tb.setKhachHang(saved.getKhachHang());
                thongBaoRepository.save(tb);

                messagingTemplate.convertAndSendToUser(
                        saved.getKhachHang().getTaiKhoan(),
                        "/topic/notification",
                        "success"
                );
                logger.info("Cập nhật trạng thái hóa đơn {} thành {}", orderId, status);
            } else {
                logger.warn("Không tìm thấy hóa đơn với ID: {}", orderId);
            }
        } catch (NumberFormatException e) {
            logger.error("Lỗi khi parse orderId: {}", orderId, e);
        }
    }

    private String getFailureStatus(String responseCode) {
        switch (responseCode) {
            case "24": return "Khách hàng hủy giao dịch";
            default:   return "Thanh toán thất bại";
        }
    }
}
