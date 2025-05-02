package com.karam.teamup.authentication.cookies;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;


@Service
public class CookieUtil {

    public void addAuthTokenCookie(HttpServletResponse response, String token) {
        // Create the HTTP-only cookie with the token
        Cookie cookie = new Cookie("auth_token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(604800);

        response.addCookie(cookie);
    }
}
