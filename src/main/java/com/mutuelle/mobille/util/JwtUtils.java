package com.mutuelle.mobille.util;

import com.mutuelle.mobille.models.AuthUser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtils {
    @Value("${jwt.secret}")
    private String jwtSecret;
    @Value("${jwt.access.expiration}") // en ms
    private long accessExpirationMs;
    @Value("${jwt.refresh.expiration}")
    private long refreshExpirationMs;

    public String generateAccessToken(AuthUser user) {
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("type", user.getUserType())
                .claim("refId", user.getUserRefId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpirationMs))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .build()
                .parseClaimsJws(token);
    }

    public boolean isTokenValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token).getBody();
        return Long.parseLong(claims.getSubject());
    }

    public String getUserTypeFromToken(String token) {
        Claims claims = parseToken(token).getBody();
        return claims.get("type", String.class);
    }

    public Long getRefIdFromToken(String token) {
        Claims claims = parseToken(token).getBody();
        return claims.get("refId", Long.class);
    }
}
