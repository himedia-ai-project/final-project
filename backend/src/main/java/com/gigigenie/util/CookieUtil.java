package com.gigigenie.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;

public class CookieUtil {

    public static void setTokenCookie(HttpServletResponse response, String name, String value,
        long mins) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
            .path("/") // CORS 설정, 모든 경로에서 접근 가능
            .httpOnly(true) // XSS 방지, JS에서 쿠키값을 읽는 것을 불가
            .secure(false)   // HTTPS, SSL 설정
            .sameSite(
                "Lax")  // CORS 설정, None: 모든 도메인에서 접근 가능, Lax: 일부 도메인에서 접근 가능
            .maxAge(mins * 60) // maxAge 설정 (초)
            .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    public static void removeTokenCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(token, "")
            .path("/")
            .httpOnly(true)
            .secure(false)
            .sameSite("Lax")
            .maxAge(0L)
            .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }
}
