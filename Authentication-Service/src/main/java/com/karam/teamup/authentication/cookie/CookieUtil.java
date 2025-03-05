package com.karam.teamup.authentication.cookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;


@Service
public class CookieUtil {

    public void addAuthTokenCookie(HttpServletResponse response, String token) {
        // Create the HTTP-only cookie with the token
        Cookie cookie = new Cookie("auth_token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);  // Make sure this is true if you're using HTTPS
        cookie.setPath("/");  // Make cookie available to all routes
        cookie.setMaxAge(3600);  // Cookie expiration time (1 hour)

        // Add the cookie to the response
        response.addCookie(cookie);
    }
}
