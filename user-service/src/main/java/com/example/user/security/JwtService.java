package com.example.user.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    /**
     * Generate a signed JWT for the given username, embedding extra claims (like role).
     */
    public String generateToken(String username, Map<String, Object> extraClaims) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .claims(extraClaims)     // custom claims (e.g., role)
                .subject(username)       // standard "sub" claim
                .issuedAt(now)           // standard "iat"
                .expiration(expiry)      // standard "exp"
                .signWith(signingKey())  // HMAC SHA-256
                .compact();
    }

    /**
     * Parse and validate a token. Returns the claims.
     * Throws JwtException if invalid/expired — caller should catch.
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extract username from a valid token.
     */
    public String extractUsername(String token) {
        return parseToken(token).getSubject();
    }

    private SecretKey signingKey() {

        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}