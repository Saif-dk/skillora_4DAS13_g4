package com.elearning.complaint.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Service
public class JwtService {

    private final SecretKey signingKey;

    public JwtService(@Value("${app.jwt.secret}") String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.signingKey = new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    /**
     * Parse a JWT token and return the claims.
     * Throws an exception if the token is invalid or expired.
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public AuthUser extractAuthUser(String token) {
        Claims claims = parseToken(token);
        String userId = claims.getSubject();
        String email = claims.get("email", String.class);
        String role = claims.get("role", String.class);
        return new AuthUser(userId, email, role);
    }
}
