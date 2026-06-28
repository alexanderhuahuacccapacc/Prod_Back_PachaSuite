package com.pachasuite.api.controller;

import com.pachasuite.api.dto.HabitacionDTO;
import com.pachasuite.api.service.HabitacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recepcion/habitaciones")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPCIONISTA')")
@RequiredArgsConstructor
@Tag(name = "Recepción – Habitaciones", description = "Listar, ver detalle y togglear amenidades (ROLE_RECEPCIONISTA y ROLE_ADMIN). Editar datos, imágenes y crear quedan solo para ROLE_ADMIN vía /api/admin/habitaciones.")
@SecurityRequirement(name = "bearerAuth")
public class RecepcionHabitacionController {

    private final HabitacionService habitacionService;

    @GetMapping
    @Operation(summary = "Listar todas las habitaciones (solo lectura)")
    public ResponseEntity<List<HabitacionDTO>> findAll() {
        return ResponseEntity.ok(habitacionService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver detalle de una habitación (solo lectura)")
    public ResponseEntity<HabitacionDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(habitacionService.findById(id));
    }

    @PutMapping("/{id}/amenidades")
    @Operation(summary = "Actualizar amenidades de una habitación (toggles). Único campo editable por recepcionista.")
    public ResponseEntity<HabitacionDTO> updateAmenidades(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> amenidades) {
        return ResponseEntity.ok(habitacionService.updateAmenidades(id, amenidades));
    }
}