package com.pachasuite.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class ReservaRequestDTO {

    @NotNull(message = "La fecha de check-in es obligatoria")
    private LocalDate checkIn;

    @NotNull(message = "La fecha de check-out es obligatoria")
    private LocalDate checkOut;

    @NotNull(message = "El ID de habitación es obligatorio")
    private Long habitacionId;

    @Min(value = 1, message = "Debe haber al menos 1 adulto")
    private int adultos = 1;

    @Min(value = 0)
    private int ninos = 0;

    @NotNull(message = "Los datos del titular son obligatorios")
    @Valid
    private TitularDTO titular;

    @Valid
    private List<AcompananteDTO> acompanantes = new ArrayList<>();

    private List<String> extrasCodigos = new ArrayList<>();

    @NotBlank(message = "El código de verificación es obligatorio")
    @Size(min = 6, max = 6, message = "El código debe tener 6 dígitos")
    private String codigoVerificacion;

    public String getEmailTitular() {
        return titular != null ? titular.getEmail() : null;
    }

    private List<HuespedDTO> huespedescache = null;

    public List<HuespedDTO> getHuespedes() {
        if (huespedescache != null) return huespedescache;

        List<HuespedDTO> lista = new ArrayList<>();

        if (titular != null) {
            HuespedDTO t = new HuespedDTO();
            t.setNombre(titular.getNombre());
            t.setApellido(titular.getApellido());
            t.setTipo("titular");
            t.setDocumentoTipo(titular.getDocumentoTipo());
            t.setDocumento(titular.getDocumento());
            t.setEdad(titular.getEdad());
            t.setSexo(titular.getSexo());
            t.setNacionalidad(titular.getNacionalidad());
            t.setEmail(titular.getEmail());
            t.setCodigoPais(titular.getCodigoPais());
            t.setTelefono(titular.getTelefono());
            t.setPeticionEspecial(titular.getPeticionEspecial());
            lista.add(t);
        }

        if (acompanantes != null) {
            for (AcompananteDTO a : acompanantes) {
                HuespedDTO h = new HuespedDTO();
                h.setNombre(a.getNombre());
                h.setApellido(a.getApellido());
                h.setTipo("acompanante");
                h.setDocumentoTipo(a.getDocumentoTipo() != null ? a.getDocumentoTipo() : "DNI");
                h.setDocumento(a.getDocumento());
                h.setEdad(a.getEdad());
                h.setSexo(a.getSexo());
                h.setNacionalidad(a.getNacionalidad() != null ? a.getNacionalidad() : "Peruana");
                h.setEmail(a.getEmail());
                h.setCodigoPais(a.getCodigoPais());
                h.setTelefono(a.getTelefono());
                h.setPeticionEspecial(a.getPeticionEspecial());
                lista.add(h);
            }
        }

        huespedescache = lista;
        return huespedescache;
    }
}