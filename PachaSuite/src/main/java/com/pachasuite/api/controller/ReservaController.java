package com.pachasuite.api.controller;

import com.pachasuite.api.dto.ReservaRequestDTO;
import com.pachasuite.api.dto.ReservaResponseDTO;
import com.pachasuite.api.service.ReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
@Tag(name = "Reservas", description = "Creación y consulta de reservas")
public class ReservaController {

    private final ReservaService reservaService;

    @PostMapping
    @Operation(summary = "Crear una nueva reserva",
            description = "Valida el código de verificación, calcula totales y crea la reserva")
    public ResponseEntity<ReservaResponseDTO> crear(
            @Valid @RequestBody ReservaRequestDTO dto) {
        ReservaResponseDTO response = reservaService.crearReserva(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/mi-reserva")
    @Operation(summary = "Obtener la reserva del usuario autenticado",
            description = "Busca por email del titular (huésped) asociado a la sesión")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ReservaResponseDTO> findMiReserva(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(reservaService.findByEmailTitular(userDetails.getUsername()));
    }

    @GetMapping("/{codigo}")
    @Operation(summary = "Consultar reserva por código",
            description = "Retorna el detalle completo de la reserva (huéspedes + extras)")
    public ResponseEntity<ReservaResponseDTO> findByCodigo(
            @PathVariable String codigo) {
        return ResponseEntity.ok(reservaService.findByCodigo(codigo));
    }
}