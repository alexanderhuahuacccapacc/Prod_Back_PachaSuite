package com.pachasuite.api.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AcompananteDTO {

    @NotBlank(message = "El nombre del acompañante es obligatorio")
    @Size(max = 100)
    private String nombre;

    @NotBlank(message = "El apellido del acompañante es obligatorio")
    @Size(max = 100)
    private String apellido;

    // Tipo de documento: DNI, Pasaporte, etc. (opcional para acompañantes)
    @Size(max = 20)
    private String documentoTipo;

    @Size(max = 50)
    private String documento;

    // Edad opcional para acompañantes (pueden ser niños)
    private Integer edad;

    @Size(max = 20)
    private String sexo;

    @Size(max = 80)
    private String nacionalidad;

    // Email opcional para acompañantes
    @Email(message = "El email no es válido")
    @Size(max = 150)
    private String email;

    @Size(max = 10)
    private String codigoPais;

    @Size(max = 30)
    private String telefono;

    @Size(max = 500)
    private String peticionEspecial;

    // Siempre es acompanante
    public String getTipo() { return "acompanante"; }
}