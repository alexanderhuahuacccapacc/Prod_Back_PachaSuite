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
}