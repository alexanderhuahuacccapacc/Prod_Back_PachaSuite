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

    @Min(value = 1, message = "Debe haber al menos 1 adulto")
    private int adultos;

    @Min(value = 0, message = "El número de niños no puede ser negativo")
    private int ninos;

    @NotNull(message = "La habitación es obligatoria")
    private Long habitacionId;

    @NotEmpty(message = "Debe incluir al menos un huésped")
    @Valid
    private List<HuespedDTO> huespedes = new ArrayList<>();

    private List<String> extrasCodigos = new ArrayList<>();

    @NotBlank(message = "El código de verificación es obligatorio")
    @Size(min = 6, max = 6, message = "El código debe tener exactamente 6 dígitos")
    private String codigoVerificacion;

    @NotBlank(message = "El email del titular es obligatorio")
    @Email(message = "Email inválido")
    private String emailTitular;

}