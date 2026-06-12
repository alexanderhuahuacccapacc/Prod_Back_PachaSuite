package com.pachasuite.api.controller;

import com.pachasuite.api.dto.AdminReservaRequestDTO;
import com.pachasuite.api.dto.EditarReservaRequestDTO;
import com.pachasuite.api.dto.ReservaResponseDTO;
import com.pachasuite.api.service.ReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

@RestController
@RequestMapping("/api/admin/reservas")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin – Reservas", description = "Gestión completa de reservas (solo ROLE_ADMIN)")
@SecurityRequirement(name = "bearerAuth")
public class AdminReservaController {

    private final ReservaService reservaService;
    @GetMapping
    public ResponseEntity<Page<ReservaResponseDTO>> findAll(
            @PageableDefault(size = 20, sort = "checkIn", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(reservaService.findAll(pageable));
    }

    @GetMapping("/{codigo}")
    @Operation(summary = "Obtener reserva por código")
    public ResponseEntity<ReservaResponseDTO> findByCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(reservaService.findByCodigo(codigo));
    }

    @PostMapping
    @Operation(summary = "Crear reserva presencial (recepción)",
            description = "Sin verificación. Estado: confirmada. Habitación: ocupada. Origen: ADMIN.")
    public ResponseEntity<ReservaResponseDTO> crear(
            @Valid @RequestBody AdminReservaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaService.crearReservaAdmin(dto));
    }

    @PutMapping("/{id}/confirmar")
    @Operation(summary = "Confirmar reserva pendiente → confirmada + habitación ocupada")
    public ResponseEntity<ReservaResponseDTO> confirmar(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.confirmar(id));
    }

    @PutMapping("/{id}/editar")
    @Operation(summary = "Editar reserva pendiente",
            description = "Permite cambiar fechas, adultos, niños, extras y pago. " +
                    "Solo funciona si estado = 'pendiente'. Re-calcula totales automáticamente.")
    public ResponseEntity<ReservaResponseDTO> editar(
            @PathVariable Long id,
            @Valid @RequestBody EditarReservaRequestDTO dto) {
        return ResponseEntity.ok(reservaService.editarReserva(id, dto));
    }

    @PutMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar reserva",
            description = "Cancela reservas 'pendiente' o 'confirmada'. " +
                    "Si no hay otras reservas activas para la habitación, vuelve a 'libre'.")
    public ResponseEntity<ReservaResponseDTO> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.cancelarReserva(id));
    }
}