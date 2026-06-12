package com.pachasuite.api.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BusquedaRequestDTO {

    @NotNull(message = "La fecha de check-in es obligatoria")
    @FutureOrPresent(message = "El check-in no puede ser en el pasado")
    private LocalDate checkIn;

    @NotNull(message = "La fecha de check-out es obligatoria")
    private LocalDate checkOut;

    @Min(value = 1, message = "Debe haber al menos 1 adulto")
    private int adultos = 1;

    @Min(value = 0, message = "El número de niños no puede ser negativo")
    private int ninos = 0;
}