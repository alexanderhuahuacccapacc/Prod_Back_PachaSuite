package com.pachasuite.api.dto;

import com.pachasuite.api.entities.RegistroCochera;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RegistroCocheraDTO {
    private Long id;
    private VehiculoDTO vehiculo;
    private EspacioCocheraDTO espacio;
    private Long usuarioId;
    private String usuarioNombre;
    private Long reservaId;
    private LocalDateTime fechaIngreso;
    private LocalDateTime fechaSalida;
    private String observacion;
    private LocalDateTime createdAt;

    public static RegistroCocheraDTO from(RegistroCochera r) {
        return RegistroCocheraDTO.builder()
                .id(r.getId())
                .vehiculo(VehiculoDTO.from(r.getVehiculo()))
                .espacio(EspacioCocheraDTO.from(r.getEspacio()))
                .usuarioId(r.getUsuario().getId())
                .usuarioNombre(r.getUsuario().getNombre())
                .reservaId(r.getReserva() != null ? r.getReserva().getId() : null)
                .fechaIngreso(r.getFechaIngreso())
                .fechaSalida(r.getFechaSalida())
                .observacion(r.getObservacion())
                .createdAt(r.getCreatedAt())
                .build();
    }
}