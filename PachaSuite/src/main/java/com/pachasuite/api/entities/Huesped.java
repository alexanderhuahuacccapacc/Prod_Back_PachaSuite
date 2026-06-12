package com.pachasuite.api.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "huespedes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Huesped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id", nullable = false)
    private Reserva reserva;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "huesped_tipo")
    @Builder.Default
    private HuespedTipo tipo = HuespedTipo.titular;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(name = "documento_tipo", length = 20)
    private String documentoTipo;

    @Column(length = 50)
    private String documento;

    private Integer edad;

    @Column(length = 20)
    private String sexo;

    @Column(length = 80)
    private String nacionalidad;

    @Column(length = 150)
    private String email;

    @Column(name = "codigo_pais", length = 10)
    private String codigoPais;

    @Column(length = 30)
    private String telefono;

    @Column(name = "peticion_especial", columnDefinition = "TEXT")
    private String peticionEspecial;

    public enum HuespedTipo {
        titular, acompanante
    }
}