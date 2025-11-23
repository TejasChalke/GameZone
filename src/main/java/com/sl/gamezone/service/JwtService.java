package com.sl.gamezone.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret; // must be at least 32 chars for HS256

    @Value("${jwt.expirationMillis}")
    private long expirationMillis;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Validate the token
    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith((SecretKey) getSigningKey())
                    .build()
                    .parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Extract username from token
    public String extractUsername(String token) {
        return getAllClaims(token).getSubject();
    }

    // Internal: parse claims
    private Claims getAllClaims(String token) {
        return (Claims) Jwts.parser()
                .verifyWith((SecretKey) getSigningKey())
                .build()
                .parse(token)
                .getPayload();
    }

    // Extract and validate
    public String validateAndExtract(String token) {
        if (!isTokenValid(token)) {
            throw new JwtException("Invalid or expired token");
        }
        return extractUsername(token);
    }
}
