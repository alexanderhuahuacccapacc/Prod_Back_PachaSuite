package com.pachasuite.api.repository;

import com.pachasuite.api.entities.Habitacion;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HabitacionRepository extends JpaRepository<Habitacion, Long> {

    // ── NUEVO: usado por HabitacionService.create() para validar número duplicado ──
    boolean existsByNumero(String numero);

    @Query("""
    SELECT h FROM Habitacion h
    WHERE h.estado = 'libre'
      AND h.capacidad >= :capacidadTotal
      AND h.capacidad <= :capacidadMax
      AND h.id NOT IN (
          SELECT r.habitacion.id FROM Reserva r
          WHERE r.estado IN ('pendiente', 'confirmada')
            AND r.checkIn  < :checkOut
            AND r.checkOut > :checkIn
      )
    ORDER BY h.capacidad ASC, h.precioBase ASC
    """)
    List<Habitacion> findDisponibles(
            @Param("checkIn")       LocalDate checkIn,
            @Param("checkOut")      LocalDate checkOut,
            @Param("capacidadTotal") int capacidadTotal,
            @Param("capacidadMax")   int capacidadMax
    );

    /**
     * Verifica si una habitación tiene reservas que se solapen con las fechas dadas,
     * excluyendo opcionalmente una reserva específica (útil en modificaciones).
     */
    @Query("""
        SELECT COUNT(r) > 0 FROM Reserva r
        WHERE r.habitacion.id = :habitacionId
          AND r.estado IN ('pendiente', 'confirmada')
          AND r.checkIn  < :checkOut
          AND r.checkOut > :checkIn
          AND (:excludeReservaId IS NULL OR r.id <> :excludeReservaId)
        """)
    boolean existeSolape(
            @Param("habitacionId")      Long habitacionId,
            @Param("checkIn")           LocalDate checkIn,
            @Param("checkOut")          LocalDate checkOut,
            @Param("excludeReservaId")  Long excludeReservaId
    );
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT h FROM Habitacion h WHERE h.id = :id")
    Optional<Habitacion> findByIdWithLock(@Param("id") Long id);
}