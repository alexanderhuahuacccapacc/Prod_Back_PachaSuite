package com.pachasuite.api.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "codigos_verificacion")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CodigoVerificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contacto_id")
    private VerificacionContacto contacto;

    @Column(nullable = false, length = 6)
    private String codigo;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MetodoVerificacion metodo;

    @Builder.Default
    @Column(nullable = false)
    private Boolean usado = false;

    @Column(name = "expira_en", nullable = false)
    private LocalDateTime expiraEn;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now(ZoneOffset.UTC);  // ← CORREGIDO
    }

    public enum MetodoVerificacion {
        EMAIL
    }
}