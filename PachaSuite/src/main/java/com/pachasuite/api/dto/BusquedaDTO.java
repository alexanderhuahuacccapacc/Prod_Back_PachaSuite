package com.pachasuite.api.dto;

import com.pachasuite.api.entities.Busqueda;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BusquedaDTO {

    private Long id;
    private String destino;
    private LocalDateTime fechaBusqueda;
    private Integer resultados;

    public static BusquedaDTO from(Busqueda busqueda) {
        return BusquedaDTO.builder()
                .id(busqueda.getId())
                .destino(busqueda.getDestino())
                .fechaBusqueda(busqueda.getFechaBusqueda())
                .resultados(busqueda.getResultados())
                .build();
    }
}