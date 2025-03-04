package com.karam.teamup.gateway.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class JwtUtil {

    @Value("${my.jwt.secret-key}")
    public String SECRET;

    // Validate the token by checking if it can be parsed and verifying its signature
    public void validateToken(final String token) {
        System.out.println("Validating token: " + token);
        Jwts.parser()
            .setSigningKey(getSignKey())
            .build()
            .parseClaimsJws(token);
    }

    // Helper method to get the signing key for validation and extraction of claims
    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Extract claims from the token (username, role, etc.)
    public Claims extractClaims(String token) {
        return Jwts.parser()
                   .setSigningKey(getSignKey())
                   .build()
                   .parseClaimsJws(token)
                   .getBody();  // Use getBody to get the claims directly
    }

    // Extract username from the token
    public String extractUsername(String token) {
        Claims claims = extractClaims(token);
        return claims.getSubject();  // "subject" is where the username is stored
    }

    // Extract role from the token
    public String extractRole(String token) {
        Claims claims = extractClaims(token);
        return claims.get("role", String.class);  // Get the role claim, should be "ROLE_USER", "ROLE_ADMIN", etc.
    }
}
