package com.ystu.schedule.security;

import com.ystu.schedule.config.JwtConfig;
import com.ystu.schedule.entity.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String CLAIM_ROLE = "role";

    private final SecretKey secretKey;
    private final long expiration;

    public JwtUtil(JwtConfig jwtConfig) {
        this.secretKey = Keys.hmacShaKeyFor(
                jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
        this.expiration = jwtConfig.getExpiration();
    }

    public String generateToken(String username, Role role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(username)
                .claim(CLAIM_ROLE, role.name())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public Role extractRole(String token) {
        String roleName = parseClaims(token).get(CLAIM_ROLE, String.class);
        return Role.valueOf(roleName);
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
