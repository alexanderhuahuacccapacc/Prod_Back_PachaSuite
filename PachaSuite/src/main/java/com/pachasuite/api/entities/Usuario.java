package com.pachasuite.api.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "usuario_rol")
    @Builder.Default
    private UsuarioRol rol = UsuarioRol.ROLE_RECEPCIONISTA;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    // ── Campos para cuentas temporales de huésped (ROLE_GUEST) ──
    // Null para ADMIN/RECEPCIONISTA. Para GUEST, marca cuándo deja de poder loguearse
    // (= checkOut de la reserva que originó la cuenta).
    @Column(name = "expira_en")
    private LocalDateTime expiraEn;

    // Referencia a la reserva que generó esta cuenta guest. Permite limitar
    // el acceso de /api/reservas/{codigo} a "solo SU propia reserva" sin tener
    // que volver a buscarla por email cada vez.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id")
    private Reserva reserva;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * true si esta cuenta es de tipo huésped temporal.
     * Lo usamos en UserDetailsServiceImpl para activar la lógica de expiración
     * y en el controller de reservas/cochera para validar pertenencia.
     */
    @Transient
    public boolean esGuest() {
        return this.rol == UsuarioRol.ROLE_GUEST;
    }

    public enum UsuarioRol {
        ROLE_ADMIN, ROLE_RECEPCIONISTA, ROLE_GUEST
    }
}