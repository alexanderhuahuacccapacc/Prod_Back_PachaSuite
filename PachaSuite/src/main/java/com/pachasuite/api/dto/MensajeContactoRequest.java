package com.pachasuite.api.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class MensajeContactoRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150)
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email inválido")
    @Size(max = 150)
    private String email;

    @Size(max = 30)
    private String telefono;

    @NotBlank(message = "El asunto es obligatorio")
    @Size(max = 200)
    private String asunto;

    @NotBlank(message = "El mensaje es obligatorio")
    @Size(max = 5000, message = "El mensaje no puede superar 5000 caracteres")
    private String mensaje;
}