package com.pachasuite.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class HabitacionUpdateDTO {

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    @DecimalMin(value = "0.01", message = "El precio base debe ser mayor a 0")
    private BigDecimal precioBase;

    private String estado;

    private Map<String, Boolean> amenidades;

    // ── Nuevos campos ──
    private String camas;      // ej. "1 cama king", "2 camas twin"
    private Integer sizeM2;    // ej. 30
}