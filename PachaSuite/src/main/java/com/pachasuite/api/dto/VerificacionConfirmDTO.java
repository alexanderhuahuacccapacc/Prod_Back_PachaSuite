package com.pachasuite.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerificacionConfirmDTO {

    @NotBlank(message = "El contacto es obligatorio")
    private String contacto;

    @NotBlank(message = "El código es obligatorio")
    private String codigo;
}