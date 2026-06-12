package com.pachasuite.api.controller;

import com.pachasuite.api.dto.MensajeContactoRequest;
import com.pachasuite.api.dto.MensajeContactoResponse;
import com.pachasuite.api.dto.RespuestaRequest;
import com.pachasuite.api.service.MensajeContactoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mensajes-contacto")
@RequiredArgsConstructor
public class MensajeContactoController {

    private final MensajeContactoService service;

    @PostMapping
    public ResponseEntity<MensajeContactoResponse> enviar(
            @Valid @RequestBody MensajeContactoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.guardar(req));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MensajeContactoResponse>> findAll() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/no-leidos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MensajeContactoResponse>> findNoLeidos() {
        return ResponseEntity.ok(service.listarNoLeidos());
    }

    @GetMapping("/no-leidos/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> countNoLeidos() {
        return ResponseEntity.ok(service.contarNoLeidos());
    }

    @PatchMapping("/{id}/leido")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeContactoResponse> marcarLeido(@PathVariable Long id) {
        return ResponseEntity.ok(service.marcarLeido(id));
    }

    @PatchMapping("/marcar-todos-leidos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> marcarTodosLeidos() {
        service.marcarTodosLeidos();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{id}/responder")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> responder(
            @PathVariable Long id,
            @RequestBody @Valid RespuestaRequest req) {
        service.responder(id, req.getCuerpo());
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/respondidos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MensajeContactoResponse>> findRespondidos() {
        return ResponseEntity.ok(service.listarRespondidos());
    }
}