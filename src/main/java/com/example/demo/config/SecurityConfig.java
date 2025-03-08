package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // Tắt CSRF để test
                .authorizeHttpRequests(auth -> auth
                        // Cho phép truy cập resource
                        .requestMatchers("/assets/**", "/css/**", "/js/**", "/images/**", "/login/**").permitAll()
                        // Cấu hình quyền truy cập
                        .requestMatchers("/admin/**", "/hoa-don/**").hasRole("ADMIN")
                        .requestMatchers("/nhanvien/**", "/san_pham/**").hasAnyRole("ADMIN", "NHAN_VIEN")
                        .requestMatchers("/shop/**").hasRole("KHACH_HANG")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)  // force redirect
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .expiredUrl("/login?expired")
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Tạm thời dùng NoOpPasswordEncoder để test
        return NoOpPasswordEncoder.getInstance();
        // Sau khi test ok thì dùng BCrypt
        // return new BCryptPasswordEncoder();
    }
}