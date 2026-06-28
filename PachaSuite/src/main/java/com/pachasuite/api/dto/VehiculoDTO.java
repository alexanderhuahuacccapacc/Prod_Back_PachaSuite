package com.pachasuite.api.dto;

import com.pachasuite.api.entities.Vehiculo;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class VehiculoDTO {
    private Long id;
    private String placa;
    private String marca;
    private String modelo;
    private String color;
    private String tipo;
    private LocalDateTime createdAt;

    public static VehiculoDTO from(Vehiculo v) {
        return VehiculoDTO.builder()
                .id(v.getId())
                .placa(v.getPlaca())
                .marca(v.getMarca())
                .modelo(v.getModelo())
                .color(v.getColor())
                .tipo(v.getTipo().name())
                .createdAt(v.getCreatedAt())
                .build();
    }
}