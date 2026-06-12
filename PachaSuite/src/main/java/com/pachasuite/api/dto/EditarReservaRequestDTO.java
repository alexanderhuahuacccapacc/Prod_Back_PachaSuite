package com.pachasuite.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class EditarReservaRequestDTO {

    @NotNull(message = "La fecha de check-in es obligatoria")
    private LocalDate checkIn;

    @NotNull(message = "La fecha de check-out es obligatoria")
    private LocalDate checkOut;

    @Min(value = 1, message = "Debe haber al menos 1 adulto")
    private int adultos;

    @Min(value = 0, message = "El número de niños no puede ser negativo")
    private int ninos;

    private List<String> extrasCodigos = new ArrayList<>();

    private String pagoEstado;
}