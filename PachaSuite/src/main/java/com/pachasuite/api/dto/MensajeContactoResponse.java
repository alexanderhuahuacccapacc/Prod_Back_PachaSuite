package com.pachasuite.api.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MensajeContactoResponse {

    private Long id;
    private String nombre;
    private String email;
    private String telefono;
    private String asunto;
    private String mensaje;
    private Boolean leido;
    private Boolean respondido;
    private LocalDateTime createdAt;
}