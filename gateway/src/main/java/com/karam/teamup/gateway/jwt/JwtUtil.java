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

    public void validateToken(final String token) {
        System.out.println("Validating token: " + token);
        Jwts.parser().verifyWith(getSignKey()).build().parseSignedClaims(token);
    }

    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                   .verifyWith(getSignKey())
                   .build()
                   .parseSignedClaims(token)
                   .getPayload();
    }

    public String extractUsername(String token) {
        Claims claims = Jwts.parser()
                            .verifyWith(getSignKey())
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();
        return claims.getSubject();
    }
}
