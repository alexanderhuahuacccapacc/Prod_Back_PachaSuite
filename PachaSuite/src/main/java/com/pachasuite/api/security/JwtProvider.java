package com.pachasuite.api.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class JwtProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getEncoder().encode(jwtSecret.getBytes());
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Authentication auth) {
        UserDetails principal = (UserDetails) auth.getPrincipal();
        // Obtener el rol
        String rol = auth.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .orElse("");
        return buildToken(principal.getUsername(), rol, true);
    }


    public String generateToken(String email) {
        return buildToken(email, "", true);  // ← agregar rol vacío
    }

    private String buildToken(String email, String rol, boolean isAuthenticated) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpiration);
        Map<String, Object> claims = new HashMap<>();
        claims.put("authenticated", isAuthenticated);
        claims.put("jti", UUID.randomUUID().toString());
        claims.put("iat", now.getTime());
        claims.put("rol", rol);  // ← AGREGAR ESTO
        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();
    }
    public String getEmailFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (Exception e) {
            log.error("Error al extraer email del token: {}", e.getMessage());
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT expirado: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("JWT no soportado: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("JWT malformado: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT vacío: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("JWT firma inválida: {}", e.getMessage());
        }
        return false;
    }

    public boolean isPreAuthToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Boolean isAuthenticated = claims.get("authenticated", Boolean.class);

            if (isAuthenticated == null || !isAuthenticated) {
                log.warn("Token pre-autenticación detectado (session fixation)");
                return true;
            }
            String jti = claims.getId();
            if (jti == null || jti.isEmpty()) {
                log.warn("Token sin JTI - posible session fixation");
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("Error verificando token pre-auth: {}", e.getMessage());
            return true;
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String email = getEmailFromToken(token);
            if (email == null || !email.equals(userDetails.getUsername())) {
                return false;
            }
            return validateToken(token) && !isPreAuthToken(token);
        } catch (Exception e) {
            return false;
        }
    }
}