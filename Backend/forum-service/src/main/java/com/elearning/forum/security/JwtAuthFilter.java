package com.elearning.forum.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT authentication filter.
 * Runs once per request, extracts and validates the Bearer token,
 * then populates the SecurityContext with an AuthUser principal.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);
        AuthUser authUser = jwtService.extractAuthUser(token);

        if (authUser != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Build authority as "ROLE_ADMIN", "ROLE_STUDENT", "ROLE_TEACHER"
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + authUser.role());

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    authUser,       // principal — available via @AuthenticationPrincipal
                    null,           // credentials — not needed for JWT
                    List.of(authority)
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            log.debug("Authenticated user: {} with role: {}", authUser.userId(), authUser.role());
        }

        filterChain.doFilter(request, response);
    }
}
