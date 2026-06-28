package com.pachasuite.api.repository;

import com.pachasuite.api.entities.RegistroCochera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistroCocheraRepository extends JpaRepository<RegistroCochera, Long> {

    @Query("SELECT r FROM RegistroCochera r WHERE r.fechaSalida IS NULL ORDER BY r.fechaIngreso DESC")
    List<RegistroCochera> findActivos();

    @Query("SELECT r FROM RegistroCochera r ORDER BY r.fechaIngreso DESC")
    List<RegistroCochera> findAllOrderByFechaIngresoDesc();

    List<RegistroCochera> findByVehiculoIdOrderByFechaIngresoDesc(Long vehiculoId);

    List<RegistroCochera> findByEspacioIdAndFechaSalidaIsNull(Long espacioId);

    // ── Para el guest: SIEMPRE filtrado por su propia reserva ──
    @Query("SELECT r FROM RegistroCochera r WHERE r.reserva.id = :reservaId ORDER BY r.fechaIngreso DESC")
    List<RegistroCochera> findByReservaId(@org.springframework.data.repository.query.Param("reservaId") Long reservaId);

    @Query("SELECT r FROM RegistroCochera r WHERE r.id = :id AND r.reserva.id = :reservaId")
    java.util.Optional<RegistroCochera> findByIdAndReservaId(
            @org.springframework.data.repository.query.Param("id") Long id,
            @org.springframework.data.repository.query.Param("reservaId") Long reservaId);
}