package com.pachasuite.api.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class TitularDTO {

    @NotBlank(message = "El nombre del titular es obligatorio")
    @Size(max = 100)
    private String nombre;

    @NotBlank(message = "El apellido del titular es obligatorio")
    @Size(max = 100)
    private String apellido;

    @NotBlank(message = "El tipo de documento es obligatorio")
    @Size(max = 20)
    private String documentoTipo;

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(max = 50)
    private String documento;

    @Min(value = 18, message = "El titular debe ser mayor de edad")
    private Integer edad;

    @Size(max = 20)
    private String sexo;

    @Size(max = 80)
    private String nacionalidad;

    @NotBlank(message = "El email del titular es obligatorio")
    @Email(message = "El email no es válido")
    @Size(max = 150)
    private String email;

    @Size(max = 10)
    private String codigoPais;

    @Size(max = 30)
    private String telefono;

    @Size(max = 500)
    private String peticionEspecial;

    // Siempre es titular
    public String getTipo() { return "titular"; }
}