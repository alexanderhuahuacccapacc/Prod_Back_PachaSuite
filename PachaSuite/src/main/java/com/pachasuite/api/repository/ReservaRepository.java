package com.pachasuite.api.repository;

import com.pachasuite.api.entities.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    Optional<Reserva> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    @Query("""
        SELECT r FROM Reserva r
        JOIN FETCH r.habitacion
        LEFT JOIN FETCH r.huespedes
        LEFT JOIN FETCH r.extras
        ORDER BY r.createdAt DESC
        """)
    Page<Reserva> findAllWithDetails(Pageable pageable);

    @Query("""
        SELECT r FROM Reserva r
        JOIN FETCH r.habitacion
        LEFT JOIN FETCH r.huespedes
        LEFT JOIN FETCH r.extras
        WHERE r.codigo = :codigo
        """)
    Optional<Reserva> findByCodigoWithDetails(@Param("codigo") String codigo);

    @Query("""
        SELECT r FROM Reserva r
        JOIN FETCH r.habitacion
        WHERE EXISTS (
            SELECT 1 FROM Huesped h WHERE h.reserva.id = r.id AND h.email = :email
        )
        ORDER BY r.createdAt DESC
        """)
    List<Reserva> findByHuespedEmail(@Param("email") String email, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT COUNT(r) > 0 FROM Reserva r WHERE r.habitacion.id = :habId " +
            "AND r.id <> :excludeId " +
            "AND r.estado IN ('pendiente', 'confirmada')")
    boolean existeOtraReservaActiva(
            @Param("habId")     Long habId,
            @Param("excludeId") Long excludeId
    );
}