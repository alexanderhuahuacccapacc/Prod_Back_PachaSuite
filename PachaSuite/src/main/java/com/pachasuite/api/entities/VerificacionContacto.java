package com.pachasuite.api.entities;

import jakarta.persistence.GeneratedValue;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.time.ZoneOffset;


@Entity
@Table(name = "verificacion_contactos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VerificacionContacto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "verificacion_tipo")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private TipoContacto tipo;

    @Column(nullable = false, length = 150)
    private String valor;

    @Column(name = "codigo_pais", length = 10)
    private String codigoPais;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now(ZoneOffset.UTC);  // ← CORREGIDO
    }

    public enum TipoContacto {
        EMAIL
    }
}