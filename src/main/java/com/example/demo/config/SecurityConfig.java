package com.example.demo.config;

import jakarta.servlet.ServletException;
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


@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login").anonymous()
                        .requestMatchers("/", "/css/**", "/js/**", "/images/**").permitAll() // Cho phép truy cập file tĩnh
                        .requestMatchers("/ban-hang-online/**").permitAll()
                        .requestMatchers("/hoa-don/**","/ban-hang-online/don-hang","/san-pham/**","/khach-hang/**","/mau-sac/**","/ban-hang-off/**").hasAnyAuthority("QUAN_LY", "NHAN_VIEN") // Nhân viên, admin vào được
                        .requestMatchers("/nhan-vien/**","/doanh-thu/**").hasAuthority("QUAN_LY")
                        .requestMatchers("/ban-hang-online/cart").hasAuthority("KHACH_HANG")// Chỉ quản lý mới vào được
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(customSuccessHandler()) // Xử lý điều hướng theo quyền
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
                                                Authentication authentication) throws IOException, ServletException {
                // Kiểm tra user có quyền nào phù hợp không
                boolean isQuanLy = authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("QUAN_LY"));

                boolean isNhanVien = authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("NHAN_VIEN"));
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
