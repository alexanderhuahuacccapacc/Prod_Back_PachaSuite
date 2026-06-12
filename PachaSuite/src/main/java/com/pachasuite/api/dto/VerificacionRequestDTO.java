package com.pachasuite.api.dto;

import com.pachasuite.api.entities.CodigoVerificacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VerificacionRequestDTO {

    @NotBlank(message = "El contacto es obligatorio (email o número de WhatsApp)")
    private String contacto;
    private String nombre;
    private String captchaToken;
}