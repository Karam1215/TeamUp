package com.karam.teamup.authentication.jwt;

import com.karam.teamup.authentication.exception.ExpiredTokenException;
import com.karam.teamup.authentication.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
public class JWTService {

    @Value("${my.jwt.secret-key}")
    private String secretKey;

    public JWTService() throws NoSuchAlgorithmException {

        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
            SecretKey sk = keyGen.generateKey();
            secretKey = Base64.getEncoder().encodeToString(sk.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new NoSuchAlgorithmException("unable to generate secret key");
        }
    }

    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 30 * 60 * 1000))
                .and()
                .signWith(getKey())
                .compact();

    }

    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUserName(String token) {
        // extract the username from jwt token
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
    try {
        final String userName = extractUserName(token);

        if (userDetails instanceof UserPrincipal userPrincipal) {
            return (userName.equals(userPrincipal.getUsername()) || userName.equals(userPrincipal.getUsername()))
                && !isTokenExpired(token);
        } else {
            throw new ClassCastException("UserDetails is not an instance of PlayerPrincipal.");
        }
    } catch (ClassCastException e) {
        // Log and handle the ClassCastException if PlayerPrincipal cast fails
        log.error("Error during token validation: {}", e.getMessage());
        return false;
    } catch (NoSuchElementException e) {
        // Handle NoSuchElementException if something is missing in the token or userDetails
        log.error("Error during token validation: {}", e.getMessage());
        return false;
    } catch (Exception e) {
        // Catch any other unexpected exceptions
        log.error("Unexpected error during token validation: {}", e.getMessage());
        return false;
    }
}

    public ResponseEntity<String> validateToken(String token){
        Jwts.parser()
                        .verifyWith(getKey())
                        .build()
                        .parseSignedClaims(token);
        if (isTokenExpired(token)){
            throw new ExpiredTokenException("The token is Expired.");
        }
        return ResponseEntity.ok("Token is Valid");
    }


    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}