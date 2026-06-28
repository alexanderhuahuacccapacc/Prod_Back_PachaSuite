package com.pachasuite.api.controller;

import com.pachasuite.api.dto.*;
import com.pachasuite.api.service.CocheraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/cochera")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin – Cochera", description = "Gestión de vehículos, espacios y registros (solo ROLE_ADMIN)")
@SecurityRequirement(name = "bearerAuth")
public class AdminCocheraController {

    private final CocheraService cocheraService;

    // ── Espacios ──────────────────────────────────────────────

    @GetMapping("/espacios")
    @Operation(summary = "Listar todos los espacios de cochera")
    public ResponseEntity<List<EspacioCocheraDTO>> listarEspacios() {
        return ResponseEntity.ok(cocheraService.listarEspacios());
    }

    @GetMapping("/espacios/{id}")
    @Operation(summary = "Obtener espacio de cochera por ID")
    public ResponseEntity<EspacioCocheraDTO> obtenerEspacio(@PathVariable Long id) {
        return ResponseEntity.ok(cocheraService.obtenerEspacio(id));
    }

    @PostMapping("/espacios")
    @Operation(summary = "Crear un nuevo espacio de cochera")
    public ResponseEntity<EspacioCocheraDTO> crearEspacio(
            @Valid @RequestBody EspacioCocheraRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cocheraService.crearEspacio(request));
    }

    @PutMapping("/espacios/{id}")
    @Operation(summary = "Actualizar un espacio de cochera")
    public ResponseEntity<EspacioCocheraDTO> actualizarEspacio(
            @PathVariable Long id,
            @Valid @RequestBody EspacioCocheraRequest request) {
        return ResponseEntity.ok(cocheraService.actualizarEspacio(id, request));
    }

    // ── Vehículos ─────────────────────────────────────────────

    @GetMapping("/vehiculos")
    @Operation(summary = "Listar todos los vehículos registrados")
    public ResponseEntity<List<VehiculoDTO>> listarVehiculos() {
        return ResponseEntity.ok(cocheraService.listarVehiculos());
    }

    @GetMapping("/vehiculos/{id}")
    @Operation(summary = "Obtener vehículo por ID")
    public ResponseEntity<VehiculoDTO> obtenerVehiculo(@PathVariable Long id) {
        return ResponseEntity.ok(cocheraService.obtenerVehiculo(id));
    }

    // ── Registros ─────────────────────────────────────────────

    @GetMapping("/registros")
    @Operation(summary = "Listar todos los registros de cochera (histórico completo)")
    public ResponseEntity<List<RegistroCocheraDTO>> listarRegistros() {
        return ResponseEntity.ok(cocheraService.listarRegistros());
    }

    @GetMapping("/registros/activos")
    @Operation(summary = "Listar solo registros activos (vehículos dentro)")
    public ResponseEntity<List<RegistroCocheraDTO>> listarActivos() {
        return ResponseEntity.ok(cocheraService.listarActivos());
    }
}