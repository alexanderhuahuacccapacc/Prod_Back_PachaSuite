package com.pachasuite.api.repository;

import com.pachasuite.api.entities.CodigoVerificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CodigoVerificacionRepository extends JpaRepository<CodigoVerificacion, Long> {

    // ELIMINA COMPLETAMENTE este método o coméntalo
    // Optional<CodigoVerificacion> findTopByContactoIdAndCodigoAndUsadoFalseAndExpiraEnAfterOrderByCreatedAtDesc(...);

    long countByContactoIdAndCreatedAtAfter(Long contactoId, LocalDateTime since);

    @Modifying
    @Query("UPDATE CodigoVerificacion c SET c.usado = true WHERE c.id = :id")
    void marcarComoUsado(@Param("id") Long id);

    // Este es el único método que debe existir para validar
    @Query("""
        SELECT c FROM CodigoVerificacion c
        JOIN c.contacto v
        WHERE v.valor = :contacto
        AND c.codigo = :codigo
        AND c.usado = false
        AND c.expiraEn > :ahora
        ORDER BY c.createdAt DESC
    """)
    Optional<CodigoVerificacion> findValidCode(
            @Param("contacto") String contacto,
            @Param("codigo")   String codigo,
            @Param("ahora")    LocalDateTime ahora
    );
}