package com.pachasuite.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);

        if (StringUtils.hasText(token)) {
            try {
                if (!isValidTokenFormat(token)) {
                    log.warn("Token con formato inválido rechazado");
                    sendErrorResponse(response, 401, "Token inválido", "INVALID_TOKEN_FORMAT");
                    return;
                }

                if (!jwtProvider.validateToken(token)) {
                    log.warn("Token inválido o expirado");
                    sendErrorResponse(response, 401, "Token inválido o expirado", "INVALID_TOKEN");
                    return;
                }

                if (jwtProvider.isPreAuthToken(token)) {
                    log.warn("SESSION FIXATION: Token pre-autenticación rechazado");
                    sendErrorResponse(response, 401,
                            "Token de sesión inválido - Inicie sesión nuevamente",
                            "SESSION_FIXATION");
                    return;
                }

                String email = jwtProvider.getEmailFromToken(token);
                if (email == null || email.isEmpty()) {
                    log.warn("Token sin email válido");
                    sendErrorResponse(response, 401, "Token inválido", "NO_EMAIL");
                    return;
                }

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                    if (jwtProvider.isTokenValid(token, userDetails)) {
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        log.debug("Usuario autenticado: {}", email);
                    } else {
                        log.warn("Token inválido para usuario: {}", email);
                        sendErrorResponse(response, 401, "Token inválido para este usuario", "INVALID_USER");
                        return;
                    }
                }

            } catch (Exception e) {
                log.error("Error procesando JWT: {}", e.getMessage());
                sendErrorResponse(response, 401, "Error de autenticación", "AUTH_ERROR");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        // 1. Primero busca en cookie HttpOnly (seguro)
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    log.debug("Token extraído desde cookie");
                    return cookie.getValue();
                }
            }
        }
        // 2. Fallback: header Authorization (para Postman)
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            log.debug("Token extraído desde header Authorization");
            return header.substring(7);
        }
        return null;
    }

    private boolean isValidTokenFormat(String token) {
        if (token == null || token.trim().isEmpty()) return false;
        String[] parts = token.split("\\.");
        if (parts.length != 3) return false;
        if (token.equalsIgnoreCase("null") || token.equalsIgnoreCase("undefined")) return false;
        return true;
    }

    private void sendErrorResponse(HttpServletResponse response, int status,
                                   String message, String code) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(String.format(
                "{\"error\": \"%s\", \"code\": \"%s\", \"timestamp\": %d}",
                message, code, System.currentTimeMillis()
        ));
    }
}