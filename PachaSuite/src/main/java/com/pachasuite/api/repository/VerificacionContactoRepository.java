package com.pachasuite.api.repository;

import com.pachasuite.api.entities.VerificacionContacto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificacionContactoRepository extends JpaRepository<VerificacionContacto, Long> {

    Optional<VerificacionContacto> findByTipoAndValor(
            VerificacionContacto.TipoContacto tipo,
            String valor);
}