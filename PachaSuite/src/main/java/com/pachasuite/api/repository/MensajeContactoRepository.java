package com.pachasuite.api.repository;

import com.pachasuite.api.entities.MensajeContacto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeContactoRepository extends JpaRepository<MensajeContacto, Long> {

    // Todos los no leídos (útil para el panel admin)
    List<MensajeContacto> findByLeidoFalseOrderByCreatedAtDesc();

    // Todos ordenados por fecha
    List<MensajeContacto> findAllByOrderByCreatedAtDesc();
    List<MensajeContacto> findByRespondidoTrueOrderByCreatedAtDesc();   // ← NUEVO

    long countByLeidoFalse();


}