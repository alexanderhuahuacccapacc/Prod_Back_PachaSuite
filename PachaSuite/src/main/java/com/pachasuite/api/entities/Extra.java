package com.pachasuite.api.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "extras")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Extra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 50)
    private String icono;

    @Column(name = "precio_noche", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioNoche;

    @ManyToMany(mappedBy = "extras")
    @Builder.Default
    private Set<Reserva> reservas = new HashSet<>();
}