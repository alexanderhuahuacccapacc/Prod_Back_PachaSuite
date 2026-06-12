package com.pachasuite.api.controller;


import com.pachasuite.api.dto.LoginDTO;
import com.pachasuite.api.dto.JwtDTO;
import com.pachasuite.api.dto.MessageDTO;
import com.pachasuite.api.dto.RegisterDTO;
import com.pachasuite.api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    @Operation(summary = "Iniciar sesión y obtener JWT")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO request) {  // ← Usar LoginDTO

        if (request.getEmail() == null || request.getEmail().trim().isEmpty() ||
                request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Email y password son requeridos",
                    "code", "MISSING_CREDENTIALS"
            ));
        }

        try {
            JwtDTO response = authService.login(request);  // ← JwtDTO, no LoginResponse
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", e.getMessage(),
                    "code", "INVALID_CREDENTIALS"
            ));
        }
    }
}