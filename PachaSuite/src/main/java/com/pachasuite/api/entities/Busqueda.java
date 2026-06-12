package com.pachasuite.api.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "busquedas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Busqueda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String destino;

    @Column(name = "fecha_busqueda")
    private LocalDateTime fechaBusqueda;

    private Integer resultados;
}