package com.pachasuite.api.controller;

import com.pachasuite.api.dto.ActividadDTO;
import com.pachasuite.api.dto.HabitacionDTO;
import com.pachasuite.api.dto.HabitacionUpdateDTO;
import com.pachasuite.api.service.HabitacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/habitaciones")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin – Habitaciones", description = "CRUD y amenidades (solo ROLE_ADMIN)")
@SecurityRequirement(name = "bearerAuth")
public class AdminHabitacionController {

    private final HabitacionService habitacionService;

    @GetMapping
    @Operation(summary = "Listar todas las habitaciones")
    public ResponseEntity<List<HabitacionDTO>> findAll() {
        return ResponseEntity.ok(habitacionService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener habitación por ID")
    public ResponseEntity<HabitacionDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(habitacionService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar habitación (nombre, precio, estado, camas, sizeM2, amenidades)")
    public ResponseEntity<HabitacionDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody HabitacionUpdateDTO dto) {
        return ResponseEntity.ok(habitacionService.update(id, dto));
    }

    @PutMapping("/{id}/amenidades")
    @Operation(summary = "Actualizar matriz de amenidades con toggles")
    public ResponseEntity<HabitacionDTO> updateAmenidades(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> amenidades) {
        return ResponseEntity.ok(habitacionService.updateAmenidades(id, amenidades));
    }

    /// GET /api/admin/habitaciones/actividad
    /// Devuelve las últimas 20 acciones registradas (más reciente primero).
    @GetMapping("/actividad")
    @Operation(summary = "Feed de actividad reciente del dashboard")
    public ResponseEntity<List<ActividadDTO>> getActividad() {
        return ResponseEntity.ok(habitacionService.getActividadReciente());
    }
}