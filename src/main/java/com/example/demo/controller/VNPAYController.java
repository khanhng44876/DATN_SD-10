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
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.stream.Collectors;
import java.util.*;
import org.slf4j.Logger;

@Controller
public class VNPAYController {

    private static final Logger logger = LoggerFactory.getLogger(VNPAYController.class);

    @Autowired
    SimpMessagingTemplate messagingTemplate;

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
    public String offlineReturn(HttpServletRequest request ,Model model) {
        String vnp_TxnRef      = request.getParameter("vnp_TxnRef");      // mã tham chiếu bạn gửi lên
        String vnp_ResponseCode= request.getParameter("vnp_ResponseCode");
        String vnp_SecureHash  = request.getParameter("vnp_SecureHash");
        String status = "fail";
        Map<String, String[]> raw = request.getParameterMap();
        Map<String, String> params = raw.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> (e.getValue()!=null && e.getValue().length>0)
                                ? e.getValue()[0]
                                : ""
                ));

        boolean valid = vnPayService.verifySignature(params, vnp_SecureHash);
        if (!valid) {
            model.addAttribute("status", "error");
            model.addAttribute("message", "Chữ ký không hợp lệ!");
            return "ban_hang_off/paymentResult";
        }

        // 3) Kiểm tra kết quả thanh toán
        if ("00".equals(vnp_ResponseCode)) {
            model.addAttribute("status", "success");
            model.addAttribute("message", "Thanh toán thành công!");
            model.addAttribute("txnRef", vnp_TxnRef);
             status = "success";
        } else {
            model.addAttribute("status", "fail");
            model.addAttribute("message",
                    "Thanh toán thất bại, mã lỗi: " + vnp_ResponseCode);
        }

        // 4) Trả về view để frontend hiển thị span + nút Xác nhận lưu hóa đơn
        return "redirect:/ban-hang-off/hien-thi?paymentResult="+ status ;
    }

    @GetMapping("/vnpay-payment-return")
    @Transactional
    public String paymentCompleted(HttpServletRequest request, Model model) {
        try {
            int paymentStatus = vnPayService.orderReturn(request);

            String orderInfo = request.getParameter("vnp_OrderInfo");
            String paymentTime = request.getParameter("vnp_PayDate");
            String transactionId = request.getParameter("vnp_TransactionNo");
            String totalPrice = request.getParameter("vnp_Amount");
            String responseCode = request.getParameter("vnp_ResponseCode");

            String orderId = extractOrderId(orderInfo);
            if (orderId == null) {
                logger.error("Không thể trích xuất orderId từ orderInfo: {}", orderInfo);
                model.addAttribute("error", "Lỗi xử lý đơn hàng: Không tìm thấy ID hóa đơn");
                return "ban_hang_online/paymentResult";
            }

            model.addAttribute("orderId", orderInfo);
            model.addAttribute("totalPrice", totalPrice != null ? Long.parseLong(totalPrice) / 100 : 0);
            model.addAttribute("paymentTime", paymentTime);
            model.addAttribute("transactionId", transactionId);
            model.addAttribute("paymentStatus", paymentStatus == 1 ? "Thành công" : getFailureStatus(responseCode));

            return "ban_hang_online/paymentResult";
        } catch (Exception e) {
            logger.error("Lỗi khi xử lý VNPay callback: {}", e.getMessage());
            model.addAttribute("error", "Lỗi xử lý thanh toán: " + e.getMessage());
            return "ban_hang_online/paymentResult";
        }
    }

    @PostMapping("/ban-hang-online/vnpay-ipn")
    @ResponseBody
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
        if (vnPayService.verifySignature(fields, vnp_SecureHash)) {
            String vnp_ResponseCode = fields.get("vnp_ResponseCode");
            String orderInfo = fields.get("vnp_OrderInfo");
            String orderId = extractOrderId(orderInfo);

            if (orderId == null) {
                response.put("RspCode", "99");
                response.put("Message", "Invalid orderId");
                return ResponseEntity.ok(response);
            }

            if ("00".equals(vnp_ResponseCode)) {
                updateOrderStatus(orderId, "Chờ xác nhận");
                HoaDon hd = hoaDonRepository.findById(Integer.parseInt(orderId))
                        .orElseThrow(()->new EntityNotFoundException("HD không tìm thấy"));
//                Trừ số lượng sản phẩm
                List<HoaDonCT> chiTiet = hoaDonCTRepository.findByHoaDonId(Integer.valueOf(orderId));
                for(HoaDonCT ct : chiTiet) {
                    SanPhamChiTiet sp = ct.getSanPhamChiTiet();
                    sp.setSoLuong(sp.getSoLuong()-ct.getSoLuong());
                    if(sp.getSoLuong() == 0){
                        sp.setTrangThai("Hết hàng");
                    }
                    sanPhamCTRepository.save(sp);
                }
//                - khuyến mãi
                KhuyenMai km = hd.getKhuyenMai();
                if(km != null){
                    km.setSo_luong(km.getSo_luong()-1);
                    km.setSo_luong_sd(km.getSo_luong_sd()+1);
                    if(km.getSo_luong() == 0){
                        km.setTrang_thai("Đã kết thúc");
                    }
                    khuyenMaiRepository.save(km);
                }

                response.put("RspCode", "00");
                response.put("Message", "Confirm Success");
            } else {
//                Không thành công thì xóa hóa đơn
                hoaDonRepository.deleteById(Integer.parseInt(orderId));
                response.put("RspCode", "99");
                response.put("Message", "Confirm Failed");
            }
        } else {
            response.put("RspCode", "97");
            response.put("Message", "Invalid signature");
        }

        return ResponseEntity.ok(response);
    }

    private String extractOrderId(String orderInfo) {
        try {
            if (orderInfo == null || orderInfo.isEmpty()) {
                logger.warn("orderInfo is null or empty");
                return null;
            }
            String[] parts = orderInfo.split(" ");
            if (parts.length < 5) {
                logger.warn("orderInfo format invalid: {}", orderInfo);
                return null;
            }
            return parts[4]; // Lấy orderId từ "Thanh toan don hang {orderId} cua KH: {idKhachHang}"
        } catch (Exception e) {
            logger.error("Lỗi khi lấy orderId từ orderInfo: {}", orderInfo, e);
            return null;
        }
    }

    private void updateOrderStatus(String orderId, String status) {
        try {
            if (orderId == null) {
                logger.warn("orderId is null, cannot update status");
                return;
            }
            Optional<HoaDon> optionalHoaDon = hoaDonRepository.findById(Integer.parseInt(orderId));
            if (optionalHoaDon.isPresent()) {
                HoaDon hoaDon = optionalHoaDon.get();
                hoaDon.setNgaySua(new Date());
                hoaDon.setTrangThaiThanhToan(status);
                HoaDon savedHoaDon =  hoaDonRepository.save(hoaDon);
                ThongBao thongBao = new ThongBao();
                if(status.equals("Chờ xác nhận")){
                    thongBao.setLink("/ban-hang-online/follow-order/"+savedHoaDon.getId());
                    thongBao.setNoi_dung("Đơn hàng HD"+savedHoaDon.getId()+"của bạn đã được đặt thành công. Đang trong trạng thái: Chờ xác nhận ");
                    thongBao.setNgayTao(new Date());
                    thongBao.setKhachHang(savedHoaDon.getKhachHang());
                }else{
                    thongBao.setLink(null);
                    thongBao.setNoi_dung("Thanh toán thất bại!");
                    thongBao.setKhachHang(savedHoaDon.getKhachHang());
                    thongBao.setNgayTao(new Date());
                }
                thongBaoRepository.save(thongBao);
                messagingTemplate.convertAndSendToUser(
                        savedHoaDon.getKhachHang().getTaiKhoan(),
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
            case "24":
                return "Khách hàng hủy giao dịch";
            default:
                return "Thanh toán thất bại";
        }
    }
}