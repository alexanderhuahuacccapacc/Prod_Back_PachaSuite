package com.pachasuite.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EspacioCocheraRequest {

    @NotBlank(message = "El código es obligatorio")
    private String codigo;

    private String ubicacion;
}