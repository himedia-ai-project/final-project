package com.gigigenie.domain.member.controller;

import com.gigigenie.domain.member.dto.JoinRequestDTO;
import com.gigigenie.domain.member.dto.LoginDTO;
import com.gigigenie.domain.member.service.MemberService;
import com.gigigenie.exception.CustomJWTException;
import com.gigigenie.props.JwtProps;
import com.gigigenie.util.CookieUtil;
import com.gigigenie.util.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final JWTUtil jwtUtil;
    private final JwtProps jwtProps;

    @Operation(summary = "회원가입")
    @PostMapping("/join")
    public ResponseEntity<?> joinMember(@Valid @RequestBody JoinRequestDTO request) {
        log.info("join: {}", request);
        memberService.join(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "이메일 중복 확인")
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> isEmailDuplicate(@RequestParam String email) {
        log.info("이메일 중복 체크 요청: {}", email);
        boolean isDuplicate = memberService.isEmailDuplicate(email);
        log.info("이메일 중복 체크 결과: {} -> {}", email, isDuplicate);
        return ResponseEntity.ok(isDuplicate);
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    static class LoginResponseDTO {
        private String id;
        private String name;
        private String role;
        private String accessToken;
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO, HttpServletResponse response) {
        log.info("login: {}", loginDTO);
        Map<String, Object> loginClaims = memberService.login(loginDTO.getId(), loginDTO.getPassword());

        String refreshToken = loginClaims.get("refreshToken").toString();
        String accessToken = loginClaims.get("accessToken").toString();

        CookieUtil.setTokenCookie(response, "refreshToken", refreshToken, jwtProps.getRefreshTokenExpirationPeriod());
        CookieUtil.setTokenCookie(response, "accessToken", accessToken, jwtProps.getAccessTokenExpirationPeriod());

        LoginResponseDTO loginResponseDTO = LoginResponseDTO.builder()
                .id(loginClaims.get("id").toString())
                .name(loginClaims.get("name").toString())
                .role(loginClaims.get("role").toString())
                .accessToken(accessToken)
                .build();

        log.info("loginResponseDTO: {}", loginResponseDTO);
        // 로그인 성공시, id, name, role 반환
        return ResponseEntity.ok(loginResponseDTO);
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        log.info("logout");

        CookieUtil.removeTokenCookie(response, "refreshToken");
        CookieUtil.removeTokenCookie(response, "accessToken");

        return ResponseEntity.ok("logout success!");
    }

    @Operation(summary = "현재 로그인 사용자 정보 조회")
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            @CookieValue(value = "accessToken", required = false) String accessToken) {

        log.info("getCurrentUser 호출, accessToken 존재: {}", accessToken != null);

        if (accessToken == null) {
            return ResponseEntity.ok().body(Map.of("isLoggedIn", false));
        }

        try {
            Map<String, Object> claims = jwtUtil.validateToken(accessToken);

            Integer id = (Integer) claims.get("id");
            String email = (String) claims.get("email");
            String name = (String) claims.get("name");
            String role = (String) claims.get("role");

            Map<String, Object> userInfo = Map.of(
                    "id", id.toString(),
                    "email", email,
                    "name", name,
                    "role", role,
                    "isLoggedIn", true
            );

            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            log.warn("토큰 검증 실패: {}", e.getMessage());
            return ResponseEntity.ok().body(Map.of("isLoggedIn", false));
        }
    }

    /**
     * 시간이 1시간 미만으로 남았는지 체크
     *
     * @param exp 만료시간
     * @return 1시간 미만이면 true, 아니면 false
     */
    private boolean checkTime(Integer exp) {

        // JWT exp를 날짜로 변환
        Date expDate = new Date((long) exp * 1000);
        // 현재 시간과의 차이 계산 - 밀리세컨즈
        long gap = expDate.getTime() - System.currentTimeMillis();
        // 분단위 계산
        long leftMin = gap / (1000 * 60);
        return leftMin < 60;
    }

    @Operation(summary = "refreshToken 검증 및 재발급")
    @GetMapping("/refresh")
    public Map<String, Object> refresh(
            @CookieValue(value = "refreshToken") String refreshToken,
            HttpServletResponse response) {

        log.info("refresh refreshToken: {}", refreshToken);

        Map<String, Object> claims;
        try {
            claims = jwtUtil.validateToken(refreshToken);
        } catch (ExpiredJwtException e) {
            claims = e.getClaims();
            log.warn("Refresh token expired, using expired claims for reissue.");
        } catch (JwtException e) {
            throw new CustomJWTException("Invalid refreshToken");
        }

        log.info("RefreshToken claims: {}", claims);

        String newAccessToken = jwtUtil.generateToken(claims, jwtProps.getAccessTokenExpirationPeriod());

        String refreshToUse = refreshToken;
        if (checkTime((Integer) claims.get("exp"))) {
            refreshToUse = jwtUtil.generateToken(claims, jwtProps.getRefreshTokenExpirationPeriod());
        }

        CookieUtil.setTokenCookie(response, "accessToken", newAccessToken, jwtProps.getAccessTokenExpirationPeriod());
        CookieUtil.setTokenCookie(response, "refreshToken", refreshToUse, jwtProps.getRefreshTokenExpirationPeriod());

        return Map.of("success", true);
    }

}
