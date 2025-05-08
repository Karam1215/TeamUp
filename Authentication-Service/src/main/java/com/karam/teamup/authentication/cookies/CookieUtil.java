package com.karam.teamup.authentication.cookies;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class CookieUtil {
    public void addAuthTokenCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from("auth_token", token)
            .httpOnly(true)       // You can set this to true for security if needed
            .secure(true)          // Set to true for HTTPS only
            .path("/")             // Available to the entire domain
            .maxAge(604800)        // 7 days expiration time
            .sameSite("None")      // Required for cross-origin cookies
            .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }
}
