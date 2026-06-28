package com.pachasuite.api.service;

import com.pachasuite.api.entities.Usuario;
import com.pachasuite.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Limpieza periódica de cuentas guest vencidas.
 *
 * Esto NO es lo que bloquea el acceso (eso ya lo hace JwtFilter/UserDetailsServiceImpl
 * comparando expiraEn en cada request). Esto es housekeeping: borra de la BD las
 * cuentas que ya nadie puede usar, para no acumular filas muertas.
 *
 * Corre cada hora. Ajusta el cron si necesitas otra frecuencia.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GuestExpirationScheduler {

    private final UsuarioRepository usuarioRepository;

    @Scheduled(cron = "0 0 * * * *") // cada hora en punto
    @Transactional
    public void limpiarGuestsVencidos() {
        List<Usuario> vencidos = usuarioRepository.findGuestsVencidos(
                Usuario.UsuarioRol.ROLE_GUEST, LocalDateTime.now());

        if (vencidos.isEmpty()) {
            return;
        }

        log.info("Eliminando {} cuenta(s) guest vencida(s)", vencidos.size());
        usuarioRepository.deleteAll(vencidos);
    }
}