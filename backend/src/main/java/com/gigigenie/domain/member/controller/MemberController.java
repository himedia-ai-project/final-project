package com.gigigenie.domain.member.controller;

import com.gigigenie.domain.member.dto.CurrentUserResponse;
import com.gigigenie.domain.member.dto.JoinDTO;
import com.gigigenie.domain.member.dto.LoginDTO;
import com.gigigenie.domain.member.dto.MemberDTO;
import com.gigigenie.domain.member.service.MemberService;
import com.gigigenie.exception.CustomJWTException;
import com.gigigenie.props.JwtProps;
import com.gigigenie.util.CookieUtil;
import com.gigigenie.util.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public ResponseEntity<?> joinMember(@Valid @RequestBody JoinDTO joinDTO) {
        memberService.join(joinDTO);
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

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginDTO loginDTO,
                                        HttpServletResponse response) {
        MemberDTO memberDTO = memberService.login(loginDTO.getId(),
                loginDTO.getPassword());

        Map<String, Object> claims = memberDTO.getClaims();

        // 토큰 생성
        String accessToken = jwtUtil.generateToken(claims,
                jwtProps.getAccessTokenExpirationPeriod());
        String refreshToken = jwtUtil.generateToken(claims,
                jwtProps.getRefreshTokenExpirationPeriod());

        // 쿠키 생성
        CookieUtil.setTokenCookie(response, "refreshToken", refreshToken,
                jwtProps.getRefreshTokenExpirationPeriod());
        CookieUtil.setTokenCookie(response, "accessToken", accessToken,
                jwtProps.getAccessTokenExpirationPeriod());

        return ResponseEntity.ok("login success!");
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        CookieUtil.removeTokenCookie(response, "accessToken");
        CookieUtil.removeTokenCookie(response, "refreshToken");
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("logout success!");
    }

    @Operation(summary = "현재 로그인 사용자 정보 조회")
    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> getCurrentUser(
            @CookieValue(value = "accessToken", required = false) String accessToken) {
        if (accessToken == null) {
            throw new CustomJWTException("MISSING_ACCESS");
        }

        Map<String, Object> claims = jwtUtil.validateToken(accessToken);

        CurrentUserResponse userInfo = CurrentUserResponse.builder()
                .email(claims.get("email").toString())
                .name(claims.get("name").toString())
                .role(claims.get("role").toString())
                .isLoggedIn(true)
                .build();

        return ResponseEntity.ok(userInfo);
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
    public ResponseEntity<String> refresh(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken == null) {
            throw new CustomJWTException("MISSING_REFRESH");
        }

        Map<String, Object> claims = jwtUtil.validateToken(refreshToken);

        String newAccessToken = jwtUtil.generateToken(claims,
                jwtProps.getAccessTokenExpirationPeriod());

        String refreshToUse = refreshToken;
        if (checkTime((Integer) claims.get("exp"))) {
            refreshToUse = jwtUtil.generateToken(claims,
                    jwtProps.getRefreshTokenExpirationPeriod());
        }

        CookieUtil.setTokenCookie(response, "accessToken", newAccessToken,
                jwtProps.getAccessTokenExpirationPeriod());
        CookieUtil.setTokenCookie(response, "refreshToken", refreshToUse,
                jwtProps.getRefreshTokenExpirationPeriod());

        return ResponseEntity.ok("토큰이 재발급되었습니다.");
    }
}