package com.pachasuite.api.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "espacios_cochera")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EspacioCochera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String codigo;

    @Column(length = 100)
    private String ubicacion;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "espacio_estado")
    @Builder.Default
    private EspacioEstado estado = EspacioEstado.LIBRE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum EspacioEstado {
        LIBRE, OCUPADO
    }
}