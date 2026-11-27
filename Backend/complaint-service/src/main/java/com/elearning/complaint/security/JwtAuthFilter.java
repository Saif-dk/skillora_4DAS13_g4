package com.elearning.complaint.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No token: let the request pass; SecurityConfig will return 401 if authentication required
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            AuthUser authUser = jwtService.extractAuthUser(token);
            String authority = "ROLE_" + authUser.role();

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    authUser,
                    null,
                    List.of(new SimpleGrantedAuthority(authority))
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception ex) {
            // Invalid or expired token: clear context and let the request pass unauthenticated
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
