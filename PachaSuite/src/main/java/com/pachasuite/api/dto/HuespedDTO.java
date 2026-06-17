package com.pachasuite.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class HuespedDTO {
    private String nombre;
    private String apellido;
    private String tipo;          // "titular" | "acompanante"
    private String documentoTipo;
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