package com.gigigenie.config;

import com.gigigenie.domain.oauth.model.CustomOAuth2User;
import com.gigigenie.domain.oauth.service.OAuth2Service;
import com.gigigenie.security.filter.JWTCheckFilter;
import com.gigigenie.security.handler.CustomAccessDeniedHandler;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final JWTCheckFilter jwtCheckFilter;
    private final OAuth2Service oAuth2Service;

    @Value("${cors.allowed-origins}")
    private String allowedOriginsString;

    @Value("${frontend.redirect.url}")
    private String FRONTEND_URL;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(httpSecurityCorsConfigurer -> {
                httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource());
            })
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    new AntPathRequestMatcher("/favicon.ico"),
                    new AntPathRequestMatcher("/v2/api-docs"),
                    new AntPathRequestMatcher("/swagger-resources/**"),
                    new AntPathRequestMatcher("/swagger-ui/**"),
                    new AntPathRequestMatcher("/webjars/**"),
                    new AntPathRequestMatcher("/v3/api-docs/**"),
                    new AntPathRequestMatcher("/h2-console/**")
                ).permitAll()
                .requestMatchers(
                    new AntPathRequestMatcher("/api/member/login"),
                    new AntPathRequestMatcher("/api/member/join"),
                    new AntPathRequestMatcher("/api/member/check-email"),
                    new AntPathRequestMatcher("/api/member/me"),
                    new AntPathRequestMatcher("/api/member/refresh"),
                    new AntPathRequestMatcher("/api/oauth2/**"),
                    new AntPathRequestMatcher("/oauth2/**"),
                    new AntPathRequestMatcher("/login/oauth2/code/**"),
                    new AntPathRequestMatcher("/api/product/search"),
                    new AntPathRequestMatcher("/api/product/list")
                ).permitAll()
                .requestMatchers(
                    new AntPathRequestMatcher("/api/member/logout"),
                    new AntPathRequestMatcher("/api/pdf/upload"),
                    new AntPathRequestMatcher("/api/chat/**"),
                    new AntPathRequestMatcher("/api/favorite/**"),
                    new AntPathRequestMatcher("/api/history/**")
                ).hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated())
            .exceptionHandling(e -> e
                // 401: 인증 없음/실패
                .authenticationEntryPoint((req, res, ex) -> {
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().print("{\"error\":\"인증 실패\"}");
                })
                // 403: 권한 없음
                .accessDeniedHandler(new CustomAccessDeniedHandler())
            )
            // 폼/OAuth2 로그인은 API에서 비활성 (리다이렉트 방지)
            .formLogin(AbstractHttpConfigurer::disable)
            .oauth2Login(AbstractHttpConfigurer::disable);

        http.oauth2Login(oauth2 -> oauth2
            .userInfoEndpoint(userInfo -> userInfo
                .userService(oAuth2Service)
            )
            .authorizationEndpoint(endpoint -> endpoint
                .baseUri("/oauth2/authorization")
            )
            .redirectionEndpoint(endpoint -> endpoint
                .baseUri("/login/oauth2/code/*")
            )
            .successHandler((request, response, authentication) -> {
                log.info("OAuth2 로그인 성공 - successHandler 호출");
                try {
                    CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
                    log.info("인증된 사용자: {}", oAuth2User.getEmail());

                    Map<String, Object> tokens = oAuth2Service.generateTokens(oAuth2User, response);
                    log.info("토큰 생성 완료, 프론트엔드로 리다이렉트: {}", FRONTEND_URL);

                    String redirectUrl = FRONTEND_URL +
                        "/oauth2-callback?id=" + oAuth2User.getMemberId() +
                        "&name=" + java.net.URLEncoder.encode(oAuth2User.getName(), "UTF-8") +
                        "&role=" + oAuth2User.getRole().name();

                    response.sendRedirect(redirectUrl);
                } catch (Exception e) {
                    log.error("OAuth2 성공 핸들러 처리 중 오류: ", e);
                    try {
                        response.sendRedirect(FRONTEND_URL + "/login?error=authentication-error");
                    } catch (IOException ex) {
                        log.error("리다이렉션 실패: ", ex);
                    }
                }
            })
        );

        http.sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtCheckFilter,
            UsernamePasswordAuthenticationFilter.class);

        http.headers(
            headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        List<String> allowedOrigins = Arrays.asList(allowedOriginsString.split(","));
        log.info("Allowed Origins: {}", allowedOrigins);
        configuration.setAllowedOrigins(allowedOrigins);

        configuration.setAllowedMethods(
            Arrays.asList("HEAD", "GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(
            Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
