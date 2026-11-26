package com.elearning.forum.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * JWT verification service only.
 * Does NOT create tokens — tokens are issued by user-service.
 */
@Slf4j
@Service
public class JwtService {

    private final SecretKey signingKey;

    public JwtService(@Value("${app.jwt.secret}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Validates the token and returns all claims.
     *
     * @param token raw JWT (without "Bearer " prefix)
     * @return Claims if valid, throws JwtException if invalid/expired
     */
    public Claims validateAndExtractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extracts userId from subject claim.
     */
    public String extractUserId(Claims claims) {
        return claims.getSubject();
    }

    /**
     * Extracts role claim (STUDENT | TEACHER | ADMIN).
     */
    public String extractRole(Claims claims) {
        return claims.get("role", String.class);
    }

    /**
     * Extracts email claim.
     */
    public String extractEmail(Claims claims) {
        return claims.get("email", String.class);
    }

    /**
     * Convenience method: validates token and returns AuthUser.
     * Returns null if the token is invalid.
     */
    public AuthUser extractAuthUser(String token) {
        try {
            Claims claims = validateAndExtractClaims(token);
            return new AuthUser(
                    extractUserId(claims),
                    extractEmail(claims),
                    extractRole(claims)
            );
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            return null;
        }
    }
}
