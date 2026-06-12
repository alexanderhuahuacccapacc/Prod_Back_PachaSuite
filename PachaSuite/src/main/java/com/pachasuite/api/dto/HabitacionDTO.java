package com.pachasuite.api.dto;

import com.pachasuite.api.entities.Habitacion;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class HabitacionDTO {
    private Long id;
    private String numero;
    private String nombre;
    private String tipo;
    private Integer capacidad;
    private BigDecimal precioBase;
    private Integer sizeM2;
    private String camas;
    private String estado;
    private Map<String, Boolean> amenidades;
    private String[] imagenes;

    public static HabitacionDTO from(Habitacion h) {
        return HabitacionDTO.builder()
                .id(h.getId())
                .numero(h.getNumero())
                .nombre(h.getNombre())
                .tipo(h.getTipo().name())
                .capacidad(h.getCapacidad())
                .precioBase(h.getPrecioBase())
                .sizeM2(h.getSizeM2())
                .camas(h.getCamas())
                .estado(h.getEstado().name())
                .amenidades(h.getAmenidades())
                .imagenes(h.getImagenes())
                .build();
    }
}