package com.example.demo.jwt;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    @Value("${jwt.access.cookie_name}")
    private String accessTokenCookieName;
    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    // Список публичных путей, которые не требуют аутентификации
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
        "/api/auth/login",
        "/api/auth/refresh",
        "/api/alerts",
        "/swagger-ui",
        "/v3/api-docs",
        "/swagger-resources",
        "/webjars"
    );

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
    @NonNull HttpServletResponse response, 
    @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        // Пропускаем публичные пути без проверки JWT
        String requestPath = request.getRequestURI();
        if (isPublicPath(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = getJwtFromCookie(request);

        if(accessToken == null || !tokenProvider.validateToken(accessToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        // get username from token
        String username = tokenProvider.getUsernameFromToken(accessToken);

        if(username == null) {
            filterChain.doFilter(request, response);
            return;
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        authenticationToken.setDetails(new WebAuthenticationDetailsSource()
        .buildDetails(request));
        SecurityContextHolder.getContext()
        .setAuthentication(authenticationToken);

        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private String getJwtFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if(cookies == null)
            return null;
        for (Cookie cookie : cookies) {
            if (accessTokenCookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
