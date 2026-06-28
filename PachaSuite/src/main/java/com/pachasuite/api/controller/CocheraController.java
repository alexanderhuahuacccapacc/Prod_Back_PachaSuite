package com.pachasuite.api.controller;

import com.pachasuite.api.dto.*;
import com.pachasuite.api.service.CocheraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cochera")
@RequiredArgsConstructor
@Tag(name = "Cochera", description = "Registro de ingreso/salida de vehículos (usuarios autenticados)")
@SecurityRequirement(name = "bearerAuth")
public class CocheraController {

    private final CocheraService cocheraService;

    @PostMapping("/ingreso")
    @Operation(summary = "Registrar ingreso de vehículo a la cochera")
    public ResponseEntity<RegistroCocheraDTO> registrarIngreso(
            @Valid @RequestBody IngresoCocheraRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cocheraService.registrarIngreso(request, userDetails.getUsername()));
    }

    @PutMapping("/{id}/salida")
    @Operation(summary = "Registrar salida de vehículo de la cochera")
    public ResponseEntity<RegistroCocheraDTO> registrarSalida(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) SalidaCocheraRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (request == null) request = new SalidaCocheraRequest();
        return ResponseEntity.ok(cocheraService.registrarSalida(id, request, userDetails.getUsername()));
    }

    @GetMapping("/registros/activos")
    @Operation(summary = "Listar registros activos (vehículos dentro de la cochera)")
    public ResponseEntity<List<RegistroCocheraDTO>> listarActivos() {
        return ResponseEntity.ok(cocheraService.listarActivos());
    }

    @GetMapping("/registros/{id}")
    @Operation(summary = "Obtener detalle de un registro de cochera")
    public ResponseEntity<RegistroCocheraDTO> obtenerRegistro(@PathVariable Long id) {
        return ResponseEntity.ok(cocheraService.obtenerRegistro(id));
    }

    @GetMapping("/espacios")
    @Operation(summary = "Listar espacios de cochera con su estado actual")
    public ResponseEntity<List<EspacioCocheraDTO>> listarEspacios() {
        return ResponseEntity.ok(cocheraService.listarEspacios());
    }
}