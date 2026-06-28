package com.pachasuite.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IngresoCocheraRequest {

    @NotBlank(message = "La placa es obligatoria")
    private String placa;

    @NotBlank(message = "La marca es obligatoria")
    private String marca;

    @NotBlank(message = "El modelo es obligatorio")
    private String modelo;

    private String color;

    private String tipo;

    @NotNull(message = "El espacio es obligatorio")
    private Long espacioId;

    private Long reservaId;

    private String observacion;
}