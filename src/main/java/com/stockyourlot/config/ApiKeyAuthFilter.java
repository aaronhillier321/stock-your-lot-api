package com.stockyourlot.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * If the request has an X-API-Key header that matches the configured key, or the literal "stock-your-lot",
 * the request is treated as authenticated (no Bearer token required).
 */
@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String API_KEY_PRINCIPAL = "api-key";
    /** Sending this value in X-API-Key bypasses user (JWT) authentication. */
    private static final String BYPASS_API_KEY = "stock-your-lot";

    private final String apiKey;

    public ApiKeyAuthFilter(@Value("${api.key:}") String apiKey) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String suppliedKey = request.getHeader(API_KEY_HEADER);
        boolean valid = BYPASS_API_KEY.equals(suppliedKey)
                || (StringUtils.hasText(apiKey) && apiKey.equals(suppliedKey));
        if (valid) {
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    API_KEY_PRINCIPAL,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_USER")));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }
}
