package com.pachasuite.api.controller;

import com.pachasuite.api.dto.LoginDTO;
import com.pachasuite.api.dto.JwtDTO;
import com.pachasuite.api.dto.MessageDTO;
import com.pachasuite.api.dto.RegisterDTO;
import com.pachasuite.api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Registro y login de usuarios")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario")
    public ResponseEntity<MessageDTO> register(@Valid @RequestBody RegisterDTO dto) {
        authService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MessageDTO.of("Usuario registrado exitosamente"));
    }

    @Operation(summary = "Iniciar sesión")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO request,
                                   HttpServletResponse response) {

        if (request.getEmail() == null || request.getEmail().trim().isEmpty() ||
                request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Email y password son requeridos",
                    "code", "MISSING_CREDENTIALS"
            ));
        }

        try {
            JwtDTO jwtDTO = authService.login(request);

            // Crear cookie HttpOnly con el token
            Cookie cookie = new Cookie("jwt", jwtDTO.getToken());
            cookie.setHttpOnly(true);
            cookie.setSecure(false);   // ← false en dev, true en prod
            cookie.setPath("/");
            cookie.setMaxAge(86400);   // 24 horas
            response.addCookie(cookie);

            // Devolver solo info del usuario, sin el token
            return ResponseEntity.ok(Map.of(
                    "email", request.getEmail(),
                    "rol",   jwtDTO.getRol() != null ? jwtDTO.getRol() : "",
                    "message", "Login exitoso"
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", e.getMessage(),
                    "code", "INVALID_CREDENTIALS"
            ));
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);  // ← false en dev
        cookie.setPath("/");
        cookie.setMaxAge(0);      // expira inmediatamente
        response.addCookie(cookie);
        return ResponseEntity.ok(Map.of("message", "Logout exitoso"));
    }
}