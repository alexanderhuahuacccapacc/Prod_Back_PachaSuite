package com.pachasuite.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pachasuite.api.entities.IdempotencyLog;
import com.pachasuite.api.repository.IdempotencyRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingResponseWrapper;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;


@Component
@RequiredArgsConstructor
public class IdempotencyInterceptor implements HandlerInterceptor {

    private final IdempotencyRepository idempotencyRepository;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {

        if (!esPostReserva(request)) return true;

        String key = request.getHeader("Idempotency-Key");
        if (key == null || key.isBlank()) return true;

        // Si ya existe → devolver respuesta cacheada directamente
        var existing = idempotencyRepository.findByIdempotencyKey(key);
        if (existing.isPresent()) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_CREATED); // 201
            response.getWriter().write(existing.get().getResponseData());
            response.getWriter().flush();
            return false;
        }

        request.setAttribute("idempotency-key", key);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler, Exception ex) {
        String key = (String) request.getAttribute("idempotency-key");
        if (key == null) return;

        if (response.getStatus() < 200 || response.getStatus() >= 300) return;

        if (response instanceof ContentCachingResponseWrapper wrapper) {
            String body = new String(wrapper.getContentAsByteArray(),
                    StandardCharsets.UTF_8);
            if (body.isBlank()) return;

            IdempotencyLog log = new IdempotencyLog();
            log.setIdempotencyKey(key);
            log.setResponseData(body);
            log.setCreatedAt(LocalDateTime.now());
            log.setExpiresAt(LocalDateTime.now().plusDays(1));
            idempotencyRepository.save(log);
        }
    }

    private boolean esPostReserva(HttpServletRequest req) {
        return req.getMethod().equals("POST") &&
                req.getRequestURI().contains("/api/reservas");
    }
}