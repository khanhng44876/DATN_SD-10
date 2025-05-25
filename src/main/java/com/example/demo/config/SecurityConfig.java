package com.example.demo.config;

import com.example.demo.entity.KhachHang;
import com.example.demo.repository.KhachHangRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Optional;


@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login","/dang-ky/**","/kich-hoat/**","/forgot-password", "/reset-password").anonymous()
                        .requestMatchers("/", "/css/**", "/js/**", "/images/**").permitAll() // Cho phép truy cập file tĩnh
                        .requestMatchers("/ban-hang-online","/ban-hang-online/cart","/ban-hang-online/dsdh-customer","/ban-hang-online/sp","/ban-hang-online/detail").permitAll()
                        .requestMatchers("/hoa-don/**","/ban-hang-online/don-hang","ban-hang-online/admin/**" ,"/san-pham/**","/khach-hang/**","/mau-sac/**","/ban-hang-off/**").hasAnyAuthority("QUAN_LY", "NHAN_VIEN") // Nhân viên, admin vào được
                        .requestMatchers("/nhan-vien/**","/thong-ke/**","/khuyen-mai/**").hasAuthority("QUAN_LY")
                        .requestMatchers("/ban-hang-online/create-order/**").hasAuthority("KHACH_HANG")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(customSuccessHandler()) // Xử lý điều hướng theo quyền
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")  // Sau khi logout, quay về trang login
                        .invalidateHttpSession(true)  // Xóa session hiện tại
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance(); // Tạm thời không mã hóa mật khẩu
    }
    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request,
                                                HttpServletResponse response,
                                                Authentication authentication) throws IOException {
                boolean isQuanLy = authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("QUAN_LY"));
                boolean isNhanVien = authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("NHAN_VIEN"));

                //Nếu là khách hàng
                if (!isQuanLy && !isNhanVien) {
                    String taiKhoan = authentication.getName();

                    // Lấy repository từ context
                    KhachHangRepository khachHangRepo = SpringContext.getBean(KhachHangRepository.class);
                    Optional<KhachHang> optionalKH = khachHangRepo.findByTaiKhoan(taiKhoan);

                    optionalKH.ifPresent(kh -> request.getSession().setAttribute("khachHang", kh));
                }

                // Giữ nguyên điều hướng
                if (isQuanLy) {
                    response.sendRedirect("/nhan-vien/hien-thi");
                } else if (isNhanVien) {
                    response.sendRedirect("/ban-hang-off/hien-thi");
                } else {
                    response.sendRedirect("/ban-hang-online");
                }
            }
        };
    }


}
