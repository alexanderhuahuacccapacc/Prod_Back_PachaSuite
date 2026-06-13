package com.pachasuite.api.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;

@Component
@Order(1)
public class RateLimitFilter implements Filter {

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long LOGIN_WINDOW_MS = 60000; // 15 minutos

    private static final int MAX_PUBLIC_ATTEMPTS = 30;
    private static final long PUBLIC_WINDOW_MS = 60000;

    private static final int MAX_VERIFY_ATTEMPTS = 5;
    private static final long VERIFY_WINDOW_MS = 12000;

    private final Cache<String, RateLimitInfo> requestCounts = Caffeine.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .maximumSize(50_000)
            .build();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            String path = httpRequest.getRequestURI();

            if (!shouldRateLimit(path)) {
                chain.doFilter(request, response);
                return;
            }

            String clientIp = getClientIp(httpRequest);
            String key = clientIp + ":" + path;

            RateLimitConfig config = getConfigForPath(path);

            RateLimitInfo info = requestCounts.get(key, k -> new RateLimitInfo());

            synchronized (info) {
                long currentTime = System.currentTimeMillis();

                if (currentTime - info.windowStart > config.windowMs) {
                    info.windowStart = currentTime;
                    info.requestCount = 0;
                }

                if (info.requestCount >= config.maxRequests) {
                    httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                    httpResponse.setContentType("application/json");

                    long remainingTime = (config.windowMs - (currentTime - info.windowStart)) / 1000;
                    long minutes = remainingTime / 60;
                    long seconds = remainingTime % 60;

                    String message = String.format(
                            "{\"error\": \"Demasiados intentos. Intente nuevamente en %d minutos y %d segundos.\", " +
                                    "\"code\": \"RATE_LIMIT_EXCEEDED\", " +
                                    "\"retryAfter\": %d}",
                            minutes, seconds, remainingTime
                    );

                    httpResponse.setHeader("Retry-After", String.valueOf(remainingTime));
                    httpResponse.getWriter().write(message);
                    return;
                }

                info.requestCount++;
            }

            addRateLimitHeaders(httpResponse, config);

            chain.doFilter(request, response);

        } catch (Exception e) {
            httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            httpResponse.getWriter().write(
                    "{\"error\": \"Error interno en rate limiting\", \"code\": \"RATE_LIMIT_ERROR\"}"
            );
        }
    }

    private boolean shouldRateLimit(String path) {
        return path.equals("/api/auth/login") ||
                path.equals("/api/auth/reset-password") ||
                path.equals("/api/public/verificacion/enviar") ||
                path.equals("/api/public/habitaciones") ||
                path.equals("/api/public/habitaciones/disponibles") ||
                path.matches("/api/reservas/\\d+");
    }

    private RateLimitConfig getConfigForPath(String path) {
        if (path.equals("/api/auth/login")) {
            return new RateLimitConfig(MAX_LOGIN_ATTEMPTS, LOGIN_WINDOW_MS);
        }
        if (path.equals("/api/auth/reset-password")) {
            return new RateLimitConfig(MAX_LOGIN_ATTEMPTS, LOGIN_WINDOW_MS);
        }
        if (path.equals("/api/public/verificacion/enviar")) {
            return new RateLimitConfig(MAX_VERIFY_ATTEMPTS, VERIFY_WINDOW_MS);
        }
        return new RateLimitConfig(MAX_PUBLIC_ATTEMPTS, PUBLIC_WINDOW_MS);
    }

    private void addRateLimitHeaders(HttpServletResponse response, RateLimitConfig config) {
        response.setHeader("X-RateLimit-Limit", String.valueOf(config.maxRequests));
        response.setHeader("X-RateLimit-Window", String.valueOf(config.windowMs / 1000));
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String cfConnectingIp = request.getHeader("CF-Connecting-IP");
        if (cfConnectingIp != null && !cfConnectingIp.isEmpty()) {
            return cfConnectingIp;
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    private static class RateLimitConfig {
        final int maxRequests;
        final long windowMs;

        RateLimitConfig(int maxRequests, long windowMs) {
            this.maxRequests = maxRequests;
            this.windowMs = windowMs;
        }
    }

    private static class RateLimitInfo {
        long windowStart = System.currentTimeMillis();
        int requestCount = 0;
    }
}
