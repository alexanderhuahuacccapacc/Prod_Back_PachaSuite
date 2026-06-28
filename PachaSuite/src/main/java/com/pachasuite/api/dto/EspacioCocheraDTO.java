package com.pachasuite.api.dto;

import com.pachasuite.api.entities.EspacioCochera;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EspacioCocheraDTO {
    private Long id;
    private String codigo;
    private String ubicacion;
    private String estado;
    private LocalDateTime createdAt;

    public static EspacioCocheraDTO from(EspacioCochera e) {
        return EspacioCocheraDTO.builder()
                .id(e.getId())
                .codigo(e.getCodigo())
                .ubicacion(e.getUbicacion())
                .estado(e.getEstado().name())
                .createdAt(e.getCreatedAt())
                .build();
    }
}