package com.gigigenie.security.filter;

import com.gigigenie.domain.member.dto.MemberDTO;
import com.gigigenie.domain.member.enums.MemberRole;
import com.gigigenie.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Log4j2
@Component
@RequiredArgsConstructor
public class JWTCheckFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        log.info("check uri: " + path);

        // Pre-flight 요청은 필터를 타지 않도록 설정
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }

        // SecurityConfig와 일치하는 경로 패턴 - permitAll() 설정된 경로들
        if (path.equals("/api/member/login") ||
            path.equals("/api/member/join") ||
            path.equals("/api/member/check-email") ||
            path.equals("/api/member/me") ||
            path.equals("/api/member/refresh") ||
            path.equals("/api/oauth2/") ||
            path.equals("/oauth2/") ||
            path.equals("/login/oauth2/code/") ||
            path.equals("/api/product/search") ||
            path.equals("/api/product/list")) {
            return true;
        }

        // 관리자 경로
        if (path.startsWith("/api/admin/member/login") ||
            path.startsWith("/api/admin/member/join") ||
            path.startsWith("/api/admin/member/refresh") ||
            path.startsWith("/api/admin/member/logout")) {
            return true;
        }

        // Swagger 및 기타 리소스 관련 경로 제외
        if (path.startsWith("/swagger-ui/") ||
            path.startsWith("/v3/api-docs") ||
            path.startsWith("/h2-console") ||
            path.startsWith("/favicon.ico")) {
            return true;
        }

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        log.info("------------------JWTCheckFilter 시작------------------");

        Cookie[] cookies = request.getCookies();
        String accessToken = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    accessToken = cookie.getValue();
                    break;
                }
            }
        }

        if (accessToken == null) {
            log.info("AccessToken 없음, 필터 통과");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Map<String, Object> claims = jwtUtil.validateToken(accessToken);

            Integer id = (Integer) claims.get("id");
            String email = (String) claims.get("email");
            String name = (String) claims.get("name");
            String role = (String) claims.get("role");
            log.info("추출된 role: {}", role);

            MemberRole memberRole = MemberRole.valueOf(role);
            MemberDTO memberDTO = new MemberDTO(id, email, "", name, memberRole);

            UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(memberDTO, null,
                    memberDTO.getAuthorities());
            log.info("인증 토큰의 권한: {}", authenticationToken.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            log.info("SecurityContext에 인증 정보 설정 완료");

            filterChain.doFilter(request, response);
            log.info("------------------JWTCheckFilter 종료------------------");
        } catch (Exception e) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println("{\"error\":\"ERROR_ACCESS_TOKEN\"}");
        }
    }

}
