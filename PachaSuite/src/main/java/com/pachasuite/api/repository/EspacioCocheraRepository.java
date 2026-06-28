package com.pachasuite.api.repository;

import com.pachasuite.api.entities.EspacioCochera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EspacioCocheraRepository extends JpaRepository<EspacioCochera, Long> {
    Optional<EspacioCochera> findByCodigo(String codigo);
    List<EspacioCochera> findByEstado(EspacioCochera.EspacioEstado estado);
    boolean existsByCodigo(String codigo);
}