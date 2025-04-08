package com.example.demo.service;

import com.example.demo.entity.KhachHang;
import com.example.demo.repository.KhachHangRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Optional;

@Service
public class KhachHangService {

    private static final Logger logger = LoggerFactory.getLogger(KhachHangService.class);

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${jwt.secret}")
    private String SECRET_KEY; // Lấy từ application.properties

    @Value("${jwt.expiration}")
    private long EXPIRATION_TIME; // Lấy từ application.properties

    // Đăng ký khách hàng
    @Transactional
    public KhachHang dangKyKhachHang(KhachHang khachHang) throws Exception {
        // Kiểm tra email đã tồn tại
        if (khachHangRepository.findByEmail(khachHang.getEmail()).isPresent()) {
            throw new Exception("Email đã được đăng ký!");
        }

        // Kiểm tra tài khoản đã tồn tại
        if (khachHangRepository.findByTaiKhoan(khachHang.getTaiKhoan()).isPresent()) {
            throw new Exception("Tài khoản đã tồn tại!");
        }

        // Mã hóa mật khẩu
        khachHang.setMatKhau(khachHang.getMatKhau());
        khachHang.setTrangThai(false); // Chưa kích hoạt

        // Lưu khách hàng
        KhachHang khachHangMoi = khachHangRepository.save(khachHang);

        // Tạo token JWT
        String token = generateJwtToken(khachHang.getEmail());

        // Gửi email xác nhận
        try {
            guiEmailXacNhan(khachHang.getEmail(), token);
        } catch (MessagingException e) {
            logger.error("Lỗi gửi email xác nhận cho {}: {}", khachHang.getEmail(), e.getMessage());
            throw new Exception("Không thể gửi email xác nhận!");
        }

        return khachHangMoi;
    }

    // Quên mật khẩu
    @Transactional
    public void quenMatKhau(String email) throws Exception {
        Optional<KhachHang> khachHangOpt = khachHangRepository.findByEmail(email);
        if (khachHangOpt.isEmpty()) {
            throw new Exception("Không tìm thấy tài khoản với email này!");
        }

        String token = generateJwtToken(email);
        try {
            guiEmailQuenMatKhau(email, token);
        } catch (MessagingException e) {
            logger.error("Lỗi gửi email quên mật khẩu cho {}: {}", email, e.getMessage());
            throw new Exception("Không thể gửi email đặt lại mật khẩu!");
        }
    }

    // Đặt lại mật khẩu
    @Transactional
    public String datLaiMatKhau(String token, String newPassword) {
        try {
            String email = validateAndGetEmailFromToken(token);
            KhachHang khachHang = khachHangRepository.findByEmail(email)
                    .orElseThrow(() -> new Exception("Không tìm thấy tài khoản!"));

            khachHang.setMatKhau(newPassword);
            khachHangRepository.save(khachHang);
            return "Mật khẩu đã được đặt lại thành công!";
        } catch (ExpiredJwtException e) {
            return "Token đã hết hạn!";
        } catch (SignatureException e) {
            return "Token không hợp lệ!";
        } catch (Exception e) {
            return "Có lỗi xảy ra: " + e.getMessage();
        }
    }

    // Kích hoạt tài khoản
    public String kichHoatTaiKhoan(String token) {
        try {
            String email = validateAndGetEmailFromToken(token);
            KhachHang khachHang = khachHangRepository.findByEmail(email)
                    .orElseThrow(() -> new Exception("Không tìm thấy tài khoản!"));

            if (khachHang.getTrangThai()) {
                return "Tài khoản đã được kích hoạt!";
            }

            khachHang.setTrangThai(true);
            khachHangRepository.save(khachHang);
            return "Kích hoạt tài khoản thành công!";
        } catch (ExpiredJwtException e) {
            return "Token đã hết hạn!";
        } catch (SignatureException e) {
            return "Token không hợp lệ!";
        } catch (Exception e) {
            return "Có lỗi xảy ra: " + e.getMessage();
        }
    }

    // Tạo JWT token
    private String generateJwtToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }

    // Xác thực và lấy email từ token
    private String validateAndGetEmailFromToken(String token) throws ExpiredJwtException, SignatureException {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Gửi email xác nhận
    private void guiEmailXacNhan(String email, String token) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(email);
        helper.setSubject("Xác nhận đăng ký tài khoản");
        helper.setText(
                "<h3>Xin chào!</h3>" +
                        "<p>Vui lòng nhấp vào liên kết dưới đây để kích hoạt tài khoản của bạn:</p>" +
                        "<a href='http://localhost:8080/kich-hoat?token=" + token + "'>Kích hoạt tài khoản</a>" +
                        "<p>Liên kết này sẽ hết hạn sau 1 giờ.</p>",
                true
        );
        mailSender.send(message);
    }

    // Gửi email quên mật khẩu
    private void guiEmailQuenMatKhau(String email, String token) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(email);
        helper.setSubject("Đặt lại mật khẩu");
        helper.setText(
                "<h3>Xin chào!</h3>" +
                        "<p>Vui lòng nhấp vào liên kết dưới đây để đặt lại mật khẩu của bạn:</p>" +
                        "<a href='http://localhost:8080/reset-password?token=" + token + "'>Đặt lại mật khẩu</a>" +
                        "<p>Liên kết này sẽ hết hạn sau 1 giờ. Nếu bạn không yêu cầu, vui lòng bỏ qua email này.</p>",
                true
        );
        mailSender.send(message);
    }
}