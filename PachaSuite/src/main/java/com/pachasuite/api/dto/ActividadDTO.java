package com.pachasuite.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/// DTO para el feed de actividad reciente del dashboard.
/// `tipo` puede ser: "update", "amenidades", "estado", "checkin", "reserva"
@Data
@AllArgsConstructor
public class ActividadDTO {
    private String titulo;
    private String subtitulo;
    private String tipo;
    private LocalDateTime fecha;
}