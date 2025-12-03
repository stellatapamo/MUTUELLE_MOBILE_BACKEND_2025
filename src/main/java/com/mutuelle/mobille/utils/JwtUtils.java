package com.mutuelle.mobille.utils;

import com.mutuelle.mobille.models.AuthUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access.expiration}")
    private long accessExpirationMs;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateAccessToken(AuthUser user) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("type", user.getUserType().name())
                .claim("refId", user.getUserRefId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    // ← MÉTHODE OBLIGATOIRE POUR LE REFRESH TOKEN
    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    public Jws<Claims> parseToken(String token) {
        return Jwts.parser()                 // ← NOUVELLE API 0.12.x
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
    }

    public boolean isTokenValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Long getUserIdFromToken(String token) {
        return Long.valueOf(parseToken(token).getPayload().getSubject());
    }

    public String getUserTypeFromToken(String token) {
        return parseToken(token).getPayload().get("type", String.class);
    }

    public Long getRefIdFromToken(String token) {
        return parseToken(token).getPayload().get("refId", Long.class);
    }
}