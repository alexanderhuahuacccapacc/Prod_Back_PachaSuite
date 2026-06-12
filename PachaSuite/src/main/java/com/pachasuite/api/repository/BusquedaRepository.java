package com.pachasuite.api.repository;

import com.pachasuite.api.entities.Busqueda;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusquedaRepository extends JpaRepository<Busqueda, Long> {
}