package com.gigigenie.domain.oauth.controller;

import com.gigigenie.domain.oauth.model.CustomOAuth2User;
import com.gigigenie.domain.oauth.service.OAuth2Service;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

    private final OAuth2Service oAuth2Service;

    @Value("${frontend.redirect.url}")
    private String FRONTEND_URL;

    /**
     * 이 엔드포인트는 OAuth2 로그인 흐름에서 직접 호출되지 않음
     * Spring Security의 OAuth2 로그인 처리기가 로그인 프로세스를 처리
     */
    @GetMapping("/callback/google")
    public void googleCallback(HttpServletResponse response) throws IOException {
        log.info("OAuth2 Google Callback 엔드포인트 직접 호출됨 - 이 경로는 일반적으로 사용되지 않음");
        response.sendRedirect(FRONTEND_URL + "/login?error=invalid-oauth-flow");
    }

    /**
     * 현재 로그인된 OAuth2 사용자의 상태를 확인
     */
    @GetMapping("/login/status")
    public ResponseEntity<Map<String, Object>> loginStatus(@AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        Map<String, Object> response = new HashMap<>();

        if (oAuth2User != null) {
            log.info("OAuth2 로그인 상태 확인: 사용자 인증됨 - {}", oAuth2User.getEmail());
            response.put("loggedIn", true);
            response.put("id", oAuth2User.getMemberId().toString());
            response.put("name", oAuth2User.getName());
            response.put("email", oAuth2User.getEmail());
            response.put("role", oAuth2User.getRole().name());
        } else {
            log.info("OAuth2 로그인 상태 확인: 사용자 인증되지 않음");
            response.put("loggedIn", false);
        }

        return ResponseEntity.ok(response);
    }
}
