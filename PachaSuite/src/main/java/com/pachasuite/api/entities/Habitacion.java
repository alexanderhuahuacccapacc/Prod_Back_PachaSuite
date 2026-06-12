package com.pachasuite.api.entities;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "habitaciones")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Habitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String numero;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "habitacion_tipo")
    private HabitacionTipo tipo;

    @Column(nullable = false)
    private Integer capacidad;

    @Column(name = "precio_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioBase;

    @Column(name = "size_m2")
    private Integer sizeM2;

    @Column(length = 100)
    private String camas;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "habitacion_estado")
    @Builder.Default
    private HabitacionEstado estado = HabitacionEstado.libre;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private Map<String, Boolean> amenidades = new HashMap<>();

    @Column(columnDefinition = "TEXT[]")
    private String[] imagenes;

    @Version
    @Column(name = "version")
    @Builder.Default
    private Integer version = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum HabitacionTipo {
        simple, doble, matrimonial, triple, cuadruple
    }

    public enum HabitacionEstado {
        libre, pendiente, ocupada, mantenimiento
    }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}