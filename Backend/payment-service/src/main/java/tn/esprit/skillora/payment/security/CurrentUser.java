package tn.esprit.skillora.payment.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Map;

/** Identity of the caller, extracted from the Keycloak JWT ("sub" claim, display name, realm roles). */
public record CurrentUser(String id, String name, boolean admin) {

    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    public static CurrentUser from(Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(a -> ROLE_ADMIN.equals(a.getAuthority()));
        String name = null;
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            Map<String, Object> claims = jwtAuthentication.getTokenAttributes();
            Object value = claims.get("name");
            if (value == null) {
                value = claims.get("preferred_username");
            }
            name = value == null ? null : value.toString();
        }
        return new CurrentUser(authentication.getName(), name, admin);
    }
}
