package com.pachasuite.api.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes_contacto")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MensajeContacto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(length = 30)
    private String telefono;

    @Column(nullable = false, length = 200)
    private String asunto;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(nullable = false)
    @Builder.Default
    private Boolean leido = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean respondido = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}