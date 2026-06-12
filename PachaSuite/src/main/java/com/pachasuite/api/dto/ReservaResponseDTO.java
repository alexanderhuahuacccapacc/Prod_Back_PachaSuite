package com.pachasuite.api.dto;

import com.pachasuite.api.entities.Huesped;
import com.pachasuite.api.entities.Reserva;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class ReservaResponseDTO {

    private Long id;
    private Long habitacionId;
    private String codigo;
    private String habitacionNombre;
    private String habitacionNumero;
    private String habitacionTipo;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer noches;
    private Integer adultos;
    private Integer ninos;
    private String estado;
    private String pagoEstado;
    private BigDecimal subtotal;
    private BigDecimal impuestos;
    private BigDecimal total;
    private String origen;
    private LocalDateTime createdAt;
    private List<HuespedResponseDTO> huespedes;
    private List<ExtraDTO> extras;

    @Data
    @Builder
    public static class HuespedResponseDTO {
        private String nombre;
        private String apellido;
        private String tipo;
        private String email;
        private String telefono;
        private String documento;
        private String documentoTipo;
        private Integer edad;
        private String sexo;
        private String nacionalidad;
        private String peticionEspecial;

        public static HuespedResponseDTO from(Huesped h) {
            return HuespedResponseDTO.builder()
                    .nombre(h.getNombre())
                    .apellido(h.getApellido())
                    .tipo(h.getTipo().name())
                    .email(h.getEmail())
                    .telefono(h.getTelefono())
                    .documento(h.getDocumento())
                    .documentoTipo(h.getDocumentoTipo())
                    .edad(h.getEdad())
                    .sexo(h.getSexo())
                    .nacionalidad(h.getNacionalidad())
                    .peticionEspecial(h.getPeticionEspecial())
                    .build();
        }
    }

    public static ReservaResponseDTO from(Reserva r) {
        return ReservaResponseDTO.builder()
                .id(r.getId())
                .habitacionId(r.getHabitacion().getId())
                .codigo(r.getCodigo())
                .habitacionNombre(r.getHabitacion().getNombre())
                .habitacionNumero(r.getHabitacion().getNumero())
                .habitacionTipo(r.getHabitacion().getTipo().name())
                .checkIn(r.getCheckIn())
                .checkOut(r.getCheckOut())
                .noches(r.getNoches())
                .adultos(r.getAdultos())
                .ninos(r.getNinos())
                .estado(r.getEstado().name())
                .pagoEstado(r.getPagoEstado().name())
                .subtotal(r.getSubtotal())
                .impuestos(r.getImpuestos())
                .total(r.getTotal())
                .origen(r.getOrigen())
                .createdAt(r.getCreatedAt())
                .huespedes(r.getHuespedes().stream()
                        .map(HuespedResponseDTO::from)
                        .collect(Collectors.toList()))
                .extras(r.getExtras().stream()
                        .map(ExtraDTO::from)
                        .collect(Collectors.toList()))
                .build();
    }
}