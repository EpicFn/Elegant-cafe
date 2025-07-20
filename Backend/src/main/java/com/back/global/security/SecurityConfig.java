package com.back.global.security;

import com.back.global.rsData.RsData;
import com.back.standard.util.Ut;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomAuthenticationFilter customAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/favicon.ico").permitAll() // 파비콘 접근 허용 (검색 엔진 최적화)
                        .requestMatchers("/h2-console/**").permitAll() // H2 콘솔 접근 허용
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()        // 상품 목록은 로그인 없어도 볼수있음
                        .requestMatchers("/api/members/login", "/api/members/logout").permitAll() // 로그인, 로그아웃은 인증 없이 허용
                        .requestMatchers(HttpMethod.POST, "/api/members/join").permitAll() // 회원 가입은 인증 없이 허용
                        .requestMatchers("/api/adm/**").hasRole("ADMIN") // 관리자 API는 ADMIN 권한이 있는 사용자만 접근 허용
                        .requestMatchers("/api/**").authenticated() // 나머지 API는 인증된 사용자만 접근 허용
                        .anyRequest().permitAll()
                )
                .csrf(AbstractHttpConfigurer::disable) // CSRF 보호 비활성화 (API 서버에서는 일반적으로 비활성화)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(AbstractHttpConfigurer::disable)
                .addFilterBefore(customAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(
                        headers -> headers
                                .frameOptions(
                                        HeadersConfigurer.FrameOptionsConfig::sameOrigin
                                )
                )
                .exceptionHandling(
                        exceptionHandling -> exceptionHandling
                                .authenticationEntryPoint(
                                        (request, response, authException) -> {
                                            response.setContentType("application/json;charset=UTF-8");

                                            response.setStatus(401);
                                            response.getWriter().write(
                                                    Ut.json.toString(
                                                            RsData.of(
                                                                    401,
                                                                    "로그인 후 이용해주세요."
                                                            )

                                                    )
                                            );
                                        }
                                )
                                .accessDeniedHandler(
                                        (request, response, accessDeniedException) -> {
                                            response.setContentType("application/json;charset=UTF-8");

                                            response.setStatus(403);
                                            response.getWriter().write(
                                                    Ut.json.toString(
                                                            RsData.of(
                                                                    403,
                                                                    "권한이 없습니다."
                                                            )
                                                    )
                                            );
                                        }
                                )
                );

        return http.build();

    }

}
