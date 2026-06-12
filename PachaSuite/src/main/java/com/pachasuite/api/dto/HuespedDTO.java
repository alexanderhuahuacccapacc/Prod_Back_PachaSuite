package com.pachasuite.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import jakarta.validation.constraints.Email;


@Data
public class HuespedDTO {
    @NotBlank
    @Size(max = 100)
    @NotBlank(message = "El nombre del huésped es obligatorio")
    private String nombre;
    @NotBlank
    @Size(max = 100)
    @NotBlank(message = "El apellido del huésped es obligatorio")
    private String apellido;

    private String tipo;
    private String documentoTipo;
    @NotBlank
    @Size(max = 20)
    private String documento;
    private Integer edad;
    private String sexo;
    private String nacionalidad;
    @Email @Size(max = 150)
    private String email;
    private String codigoPais;
    @Size(max = 30)
    private String telefono;
    @Size(max = 500)
    private String peticionEspecial;
}