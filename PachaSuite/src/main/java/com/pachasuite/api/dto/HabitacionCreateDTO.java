package com.pachasuite.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class HabitacionCreateDTO {

    @NotBlank(message = "El número de habitación es obligatorio")
    private String numero;

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;

    @NotBlank(message = "El tipo es obligatorio")
    private String tipo;          // ej. "simple", "doble", "matrimonial", "triple", "cuadruple"

    @Min(value = 1, message = "La capacidad debe ser al menos 1")
    private int capacidad;

    @DecimalMin(value = "0.01", message = "El precio base debe ser mayor a 0")
    private BigDecimal precioBase;

    private String camas;     // opcional, ej. "1 cama king"
    private Integer sizeM2;   // opcional, ej. 30
}