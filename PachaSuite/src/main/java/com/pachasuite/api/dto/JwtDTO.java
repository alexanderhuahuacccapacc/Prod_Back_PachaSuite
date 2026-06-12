package com.pachasuite.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtDTO {
    private String token;
    private String tipo;
    private String email;
    private String nombre;
    private String rol;

    public static JwtDTO of(String token, String email, String nombre, String rol) {
        return JwtDTO.builder()
                .token(token)
                .tipo("Bearer")
                .email(email)
                .nombre(nombre)
                .rol(rol)
                .build();
    }
}