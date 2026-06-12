package com.pachasuite.api.dto;

import com.pachasuite.api.entities.Extra;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ExtraDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private String icono;
    private BigDecimal precioNoche;

    public static ExtraDTO from(Extra e) {
        return ExtraDTO.builder()
                .id(e.getId())
                .codigo(e.getCodigo())
                .nombre(e.getNombre())
                .icono(e.getIcono())
                .precioNoche(e.getPrecioNoche())
                .build();
    }
}