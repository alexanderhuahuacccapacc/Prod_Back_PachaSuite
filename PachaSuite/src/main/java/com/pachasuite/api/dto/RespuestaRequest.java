package com.pachasuite.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class RespuestaRequest {
    @NotBlank
    private String cuerpo;
}