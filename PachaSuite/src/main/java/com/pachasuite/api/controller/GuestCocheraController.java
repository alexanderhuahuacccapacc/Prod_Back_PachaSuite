package com.pachasuite.api.controller;

import com.pachasuite.api.dto.EspacioCocheraDTO;
import com.pachasuite.api.dto.IngresoCocheraRequest;
import com.pachasuite.api.dto.RegistroCocheraDTO;
import com.pachasuite.api.dto.SalidaCocheraRequest;
import com.pachasuite.api.entities.Usuario;
import com.pachasuite.api.exception.BadRequestException;
import com.pachasuite.api.security.UserDetailsServiceImpl;
import com.pachasuite.api.service.CocheraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints de cochera EXCLUSIVOS para la cuenta temporal de huésped (ROLE_GUEST).
 *
 * Diferencia clave frente a CocheraController (el de staff):
 * - El reservaId NUNCA se toma del request/body. Siempre se resuelve desde
 *   el Usuario autenticado (usuario.getReserva()). Así un guest no puede
 *   ver/modificar el vehículo de otra reserva cambiando un id.
 * - Solo expone lo que el huésped necesita: registrar su vehículo, ver su
 *   propio historial, marcar su salida, y ver espacios libres.
 * - NO expone /registros/activos (de todos), /espacios con ocupados de otros,
 *   ni edición de espacios — eso sigue siendo solo para staff.
 */
@RestController
@RequestMapping("/api/cochera/guest")
@RequiredArgsConstructor
@Tag(name = "Cochera Guest", description = "Registro de vehículo para huésped con cuenta temporal")
@SecurityRequirement(name = "bearerAuth")
public class GuestCocheraController {

    private final CocheraService cocheraService;

    @PostMapping("/ingreso")
    @Operation(summary = "Registrar el ingreso del vehículo del huésped autenticado")
    public ResponseEntity<RegistroCocheraDTO> registrarIngreso(
            @Valid @RequestBody IngresoCocheraRequest request,
            @AuthenticationPrincipal UserDetailsServiceImpl.UsuarioUserDetails principal) {

        Long reservaId = reservaIdDe(principal);
        return ResponseEntity.ok(
                cocheraService.registrarIngresoGuest(request, principal.getUsername(), reservaId));
    }

    @PutMapping("/{id}/salida")
    @Operation(summary = "Registrar la salida del vehículo del huésped (solo si es de su reserva)")
    public ResponseEntity<RegistroCocheraDTO> registrarSalida(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) SalidaCocheraRequest request,
            @AuthenticationPrincipal UserDetailsServiceImpl.UsuarioUserDetails principal) {

        Long reservaId = reservaIdDe(principal);
        if (request == null) request = new SalidaCocheraRequest();
        return ResponseEntity.ok(
                cocheraService.registrarSalidaGuest(id, request, principal.getUsername(), reservaId));
    }

    @GetMapping("/mis-registros")
    @Operation(summary = "Listar los registros de cochera de la reserva del huésped autenticado")
    public ResponseEntity<List<RegistroCocheraDTO>> misRegistros(
            @AuthenticationPrincipal UserDetailsServiceImpl.UsuarioUserDetails principal) {

        Long reservaId = reservaIdDe(principal);
        return ResponseEntity.ok(cocheraService.listarMisRegistros(reservaId));
    }

    @GetMapping("/espacios-disponibles")
    @Operation(summary = "Listar espacios de cochera actualmente libres")
    public ResponseEntity<List<EspacioCocheraDTO>> espaciosDisponibles() {
        return ResponseEntity.ok(cocheraService.listarEspaciosDisponiblesGuest());
    }

    private Long reservaIdDe(UserDetailsServiceImpl.UsuarioUserDetails principal) {
        Usuario usuario = principal.getUsuario();
        if (usuario.getReserva() == null) {
            throw new BadRequestException("Esta cuenta no tiene una reserva asociada.");
        }
        return usuario.getReserva().getId();
    }
}