package com.pachasuite.api.repository;

import com.pachasuite.api.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    // Todas las cuentas GUEST cuya fecha de expiración ya pasó.
    // Usado por el scheduler de limpieza (GuestExpirationScheduler).
    @Query("""
        SELECT u FROM Usuario u
        WHERE u.rol = :rol
        AND u.expiraEn < :ahora
        """)
    List<Usuario> findGuestsVencidos(@Param("rol") Usuario.UsuarioRol rol,
                                     @Param("ahora") LocalDateTime ahora);
}